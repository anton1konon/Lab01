import anton.ukma.repository.DaoService;
import anton.ukma.udp.StoreClientUDP;
import anton.ukma.udp.StoreServerUDP;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UDPTest {

    private static DaoService daoService;

    @BeforeAll
    public static void setup() throws SQLException, SocketException {
        new StoreServerUDP().start();
        DaoService.initialization();
        daoService = new DaoService();
        daoService.dropAllTables();
        DaoService.initialization();
        daoService.createGroup("group1");
        daoService.createGroup("group2");
        daoService.createProduct("product1", 25.12, 5, 1);
        daoService.createProduct("product2", 12.11, 5, 2);
        daoService.createProduct("product3", 10.99, 6, 2);
        daoService.createProduct("product4", 5.21, 7, 1);
        daoService.createProduct("product5", 3.12, 20, 2);
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

    @AfterAll
    public static void drop() throws SQLException {
//        daoService.dropAllTables();
    }
}