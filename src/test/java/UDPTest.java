import anton.ukma.tcp.StoreClientTCP;
import anton.ukma.udp.StoreClientUDP;
import anton.ukma.udp.StoreServerUDP;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class UDPTest {

    @Before
    public void setup() throws IOException {
        new StoreServerUDP().start();
    }

    @Test
    public void whenCanSendAndReceivePacket_thenCorrect() throws IOException, InterruptedException {

        Thread client1 = new Thread(() -> {
            String answer = null;
            try {
                StoreClientUDP client = new StoreClientUDP();
                JSONObject jo = new JSONObject();
                jo.put("productId", 5);
                int cType = 1;
                String testStr = jo.toString();
                answer = client.sendMessage(testStr, cType, 1);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertTrue(answer.contains("\"response\":200"));
        });
        client1.start();

        Thread client2 = new Thread(() -> {
            String answer = null;
            try {
                StoreClientUDP client = new StoreClientUDP();
                JSONObject jo = new JSONObject();
                jo.put("productId", 5L);
                jo.put("amount", 7);
                int cType = 2;
                String testStr = jo.toString();
                answer = client.sendMessage(testStr, cType, 1);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertEquals(answer, "{\"response\":200}");
        });
        client2.start();

        Thread client3 = new Thread(() -> {
            String answer = null;
            try {
                StoreClientUDP client = new StoreClientUDP();
                JSONObject jo = new JSONObject();
                int cType = 4;
                String testStr = jo.toString();
                answer = client.sendMessage(testStr, cType, 1);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertEquals(answer, "{\"response\":200}");
        });
        client3.start();


        client1.join();
        client2.join();
        client3.join();


    }

//    @After
//    public void tearDown() throws IOException {
//        client.sendEcho("end");
//    }
}