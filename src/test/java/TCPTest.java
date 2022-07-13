import anton.ukma.repository.DaoService;
import anton.ukma.tcp.StoreClientTCP;
import anton.ukma.tcp.StoreServerTCP;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TCPTest {

    private static DaoService daoService;

    @BeforeAll
    public static void setup() throws SQLException {
        new StoreServerTCP().start();
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

    // тест не буде працювати, так як через потоки транзакції фейляться
    @Test
    public void whenCanSendAndReceivePacket_thenCorrect() throws IOException, InterruptedException {
        Thread client1 = new Thread(() -> {
            try {
                StoreClientTCP client = new StoreClientTCP();
                JSONObject jo = new JSONObject();
                jo.put("name", "product5");
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
                jo.put("name", "product5");
                jo.put("amount", 7);
                int cType = 2;
                String testStr = jo.toString();
                client.startConnection(6666);
                String answer = client.sendMessage(testStr, cType, 1);
                assertEquals(answer, "{\"response\":200}");
//                assertEquals(ProductRepository.getProductById(5L).getAmount(), 13L); // 20 - 7 = 13

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        client2.start();

        Thread client3 = new Thread(() -> {
            try {
                StoreClientTCP client = new StoreClientTCP();
                JSONObject jo = new JSONObject();
                jo.put("name", "group3");
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
                jo.put("name", "product1");
                jo.put("groupId", "2");
                String testStr = jo.toString();
                StoreClientTCP client = new StoreClientTCP();
                int cType = 5;
                client.startConnection(6666);
                String answer = client.sendMessage(testStr, cType, 1);
                assertEquals(answer, "{\"response\":200}");
//                assertEquals(ProductGroupRepository.getAmount(), 3);
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

    @AfterAll
    public static void drop() throws SQLException {
//        daoService.dropAllTables();
    }


}
