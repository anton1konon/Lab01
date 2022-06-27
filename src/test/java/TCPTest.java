import anton.ukma.repository.ProductGroupRepository;
import anton.ukma.repository.ProductRepository;
import anton.ukma.tcp.StoreClientTCP;
import anton.ukma.tcp.StoreServerTCP;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TCPTest {

    @Before
    public void setup() {
        new StoreServerTCP().start();
    }

    @Test
    public void whenCanSendAndReceivePacket_thenCorrect() throws IOException, InterruptedException {
        Thread client1 = new Thread(() -> {
            try {
                StoreClientTCP client = new StoreClientTCP();
                JSONObject jo = new JSONObject();
                jo.put("productId", 5);
                int cType = 1;
                String testStr = jo.toString();
                client.startConnection(6666);
                String answer = client.sendMessage(testStr, cType, 1);
                assertTrue(answer.contains("\"response\":200"));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        client1.start();

        Thread client2 = new Thread(() -> {
            try {
                StoreClientTCP client = new StoreClientTCP();
                JSONObject jo = new JSONObject();
                jo.put("productId", 5L);
                jo.put("amount", 7);
                int cType = 2;
                String testStr = jo.toString();
                client.startConnection(6666);
                String answer = client.sendMessage(testStr, cType, 1);
                assertEquals(answer, "{\"response\":200}");
                assertEquals(ProductRepository.getProductById(5L).getAmount(), 13L); // 20 - 7 = 13

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        client2.start();

        Thread client3 = new Thread(() -> {
            try {
                StoreClientTCP client = new StoreClientTCP();
                JSONObject jo = new JSONObject();
                int cType = 4;
                String testStr = jo.toString();
                client.startConnection(6666);
                String answer = client.sendMessage(testStr, cType, 1);
                assertEquals(answer, "{\"response\":200}");
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        client3.start();

        Thread client4 = new Thread(() -> {
            try {
                JSONObject jo = new JSONObject();
                jo.put("groupId", 1);
                jo.put("productName", "product1");
                String testStr = jo.toString();
                StoreClientTCP client = new StoreClientTCP();
                int cType = 5;
                client.startConnection(6666);
                String answer = client.sendMessage(testStr, cType, 1);
                assertEquals(answer, "{\"response\":200}");
                assertEquals(ProductGroupRepository.getAmount(), 3);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        client4.start();

        client1.join();
        client2.join();
        client3.join();
        client4.join();

    }



}
