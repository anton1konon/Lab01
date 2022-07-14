import anton.ukma.http.MyHttpServer;
import anton.ukma.model.Product;
import anton.ukma.packet.PacketCreator;
import anton.ukma.packet.PacketReceiver;
import anton.ukma.repository.DaoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class MyHttpServerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static DaoService daoService;

    private static String token;

    private static HttpClient client;

    private static byte[] extractMessageFromPackage(byte[] body) {
        try {
            PacketReceiver packet = new PacketReceiver(body);
            return packet.getMessageStrBytes();
        } catch (InterruptedException | NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }


    private static byte[] writeMessageIntoPacket(byte[] message) {
        PacketCreator pc = new PacketCreator(message);
        return pc.getPacketBytes();
    }

    @BeforeAll
    public static void init() throws IOException, SQLException {
        MyHttpServer httpServer = new MyHttpServer();
        client = HttpClient.newHttpClient();
        DaoService.initialization();
        daoService = new DaoService();
        daoService.dropAllTables();
        DaoService.initialization();
        daoService.createGroup("testGroup1");
        daoService.createGroup("testGroup2");
        daoService.createProduct("test1", 25.21, 5, 2);
        daoService.createProduct("test2", 27.21, 8, 1);
        daoService.createProduct("test3", 35.52, 9, 2);
        daoService.createProduct("test4", 35.52, 9, 2);
        daoService.createProduct("test5", 35.52, 9, 2);
    }

    @Test
    @Order(1)
    public void testLogin() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8765/login"))
                .POST(HttpRequest.BodyPublishers.ofByteArray(
                        writeMessageIntoPacket(
                                OBJECT_MAPPER.writeValueAsBytes(Map.of(
                                        "login", "user",
                                        "password", "ee11cbb19052e40b07aac0ca060c23ee")))))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        var body = extractMessageFromPackage(response.body());
        JsonNode jsonNode = OBJECT_MAPPER.readTree(body);
        token = jsonNode.get("token").asText();
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @Order(2)
    public void testGettingAllProducts() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8765/api/product"))
                .headers("token", token)
                .GET()
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        var body = new String(extractMessageFromPackage(response.body()));


        assertEquals(response.statusCode(), 200);

        assertTrue(body.contains("{\"id\":1,\"name\":\"test1\""));
        assertTrue(body.contains("{\"id\":2,\"name\":\"test2\""));
        assertTrue(body.contains("{\"id\":3,\"name\":\"test3\""));
        assertTrue(body.contains("{\"id\":4,\"name\":\"test4\""));
        assertTrue(body.contains("{\"id\":5,\"name\":\"test5\""));
    }

    @Test
    @Order(3)
    public void testGettingOneProduct() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8765/api/product/1"))
                .GET()
                .headers("token", token)
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        byte[] body = extractMessageFromPackage(response.body());
        String bodyStr = new String(body);

        assertEquals(response.statusCode(), 200);
        assertTrue(bodyStr.contains("{\"id\":1,\"name\":\"test1\""));
    }

    @Test
    @Order(4)
    public void testGettingOneProductWithWrongID() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8765/api/product/99"))
                .headers("token", token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 404);
    }

    @Test
    @Order(5)
    public void testCreatingOneProduct() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8765/api/product/"))
                .headers("token", token)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(
                        writeMessageIntoPacket(
                                OBJECT_MAPPER.writeValueAsBytes(
                                        new Product("test_test", 25.25, 5, 1)))))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(5, daoService.getAmountOfProduct("test_test"));
    }

    @Test
    @Order(6)
    public void testDeletingOneProduct() throws URISyntaxException, IOException, InterruptedException {
        assertNotNull(daoService.findProductById(6));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8765/api/product/6"))
                .headers("token", token)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode());
        assertNull(daoService.findProductById(6));
    }

    @Test
    @Order(7)
    public void testUpdatingOneProduct() throws URISyntaxException, IOException, InterruptedException {
        assertNotEquals(daoService.findProductById(1).getName(), "newTest1");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8765/api/product/1"))
                .headers("token", token)
                .POST(HttpRequest.BodyPublishers.ofByteArray(
                        writeMessageIntoPacket(
                                OBJECT_MAPPER.writeValueAsBytes(new Product(
                                        "newTest1", 2.2, 5, 1
                                )))))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode());
        assertEquals(daoService.findProductById(1).getName(), "newTest1");
    }
}