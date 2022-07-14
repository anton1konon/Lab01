package anton.ukma.http;

import anton.ukma.model.Product;
import anton.ukma.model.ProductGroup;
import anton.ukma.packet.PacketCreator;
import anton.ukma.packet.PacketReceiver;
import anton.ukma.repository.DaoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MyHttpServer extends Thread {

    public static void main(String[] args) throws IOException {
        new MyHttpServer();
    }

    private static final DaoService daoService = new DaoService();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final TemplateEngine templateEngine;

    static {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setPrefix("/");
        resolver.setSuffix(".html");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }


    public MyHttpServer() throws IOException {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8765), 0);

        HttpContext context = server.createContext("/", new EchoHandler());
        HttpContext contextLogin = server.createContext("/login", new EchoHandler());

        context.setAuthenticator(new Auth());

        server.setExecutor(null);
        server.start();
        this.start();
    }

    static class EchoHandler implements HttpHandler {
        private final List<EndpointHandler> handlers = List.of(
                new EndpointHandler("/api/product/?", "GET", this::processGetAllProducts),
//                new EndpointHandler("/product/?", "GET", this::GetAll),

                new EndpointHandler("/api/product/?", "PUT", this::processCreateProduct),

                new EndpointHandler("/api/product/(\\d+)", "GET", this::processGetProductById),

                new EndpointHandler("/api/product/(\\d+)", "POST", this::processUpdateProduct),

                new EndpointHandler("/api/product/(\\d+)", "DELETE", this::processDeleteProduct),

                new EndpointHandler("/login", "POST", this::processLogin),

                new EndpointHandler("/api/group/?", "GET", this::processGetAllGroups),

                new EndpointHandler("/api/group/?", "PUT", this::processCreateGroup),

                new EndpointHandler("/api/group/(\\d+)", "GET", this::processGetGroupById),

                new EndpointHandler("/api/group/(\\d+)", "POST", this::processUpdateGroup),

                new EndpointHandler("/api/group/(\\d+)", "DELETE", this::processDeleteGroup)

                );

        private static byte[] extractMessageFromPackage(InputStream inputStream) {
            try {
                byte[] body = inputStream.readAllBytes();
                PacketReceiver packet = new PacketReceiver(body);
                return packet.getMessageStrBytes();
            } catch (IOException | InterruptedException | NoSuchPaddingException | NoSuchAlgorithmException |
                     InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }

        private static byte[] writeMessageIntoPacket(byte[] message) {
            PacketCreator pc = new PacketCreator(message);
            return pc.getPacketBytes();
        }

        private void processDeleteGroup(HttpExchange exchange) {
            String idStr = exchange.getRequestURI().getPath()
                    .replace("/api/group/", "")
                    .replace("/", "");
            int id = Integer.parseInt(idStr);
            try {
                int resp = daoService.deleteGroup(id);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(resp != 0 ? 204 : 404, 0);
                exchange.getResponseBody().close();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }

        private void processUpdateGroup(HttpExchange exchange) {
            try {
                String idStr = exchange.getRequestURI().getPath()
                        .replace("/api/group/", "")
                        .replace("/", "");
                int id = Integer.parseInt(idStr);

                InputStream requestBody = exchange.getRequestBody();
                byte[] message = extractMessageFromPackage(requestBody);

                ProductGroup group = OBJECT_MAPPER.readValue(message, ProductGroup.class);
                group.setId(id);
                int resp = daoService.updateGroup(group);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(resp != 0 ? 204 : 404, 0);
                exchange.getResponseBody().close();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }

        private void processGetGroupById(HttpExchange exchange) {
            String idStr = exchange.getRequestURI().getPath()
                    .replace("/api/group/", "")
                    .replace("/", "");
            int id = Integer.parseInt(idStr);
            ProductGroup group = daoService.findGroupById(id);
            if (group == null) {
                writeData("Product not found".getBytes(), 404, exchange, false);
            }
            try {
                byte[] data = writeMessageIntoPacket(OBJECT_MAPPER.writeValueAsBytes(group));
                writeData(data, 200, exchange);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        private void processCreateGroup(HttpExchange exchange) {
            try {
                ProductGroup group = OBJECT_MAPPER
                        .readValue(extractMessageFromPackage(exchange.getRequestBody()), ProductGroup.class);
                int resp = daoService.createGroup(group.getName());
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(resp != 0 ? 201 : 409, 0);
                exchange.getResponseBody().close();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }

        private void processGetAllGroups(HttpExchange exchange) {
            List<ProductGroup> products = daoService.findAllGroups();
            try {
                byte[] data = writeMessageIntoPacket(OBJECT_MAPPER.writeValueAsBytes(products));
                writeData(data, 200, exchange);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handlers.stream()
                    .filter((e) -> e.isMatch(exchange)).findFirst()
                    .ifPresentOrElse(e -> e.handle(exchange), process404(exchange));
        }

        private Runnable process404(HttpExchange exchange) {
            return () -> {
                String result = "404 error";
                byte[] data = result.getBytes();
                try {
                    exchange.sendResponseHeaders(404, data.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(data);
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
        }

        private void processLogin(HttpExchange exchange) {
            InputStream stream = exchange.getRequestBody();
            byte[] body = extractMessageFromPackage(stream);
            try {
                User user = OBJECT_MAPPER.readValue(body, User.class);
                String login = user.getLogin();
                String passwordHash = user.getPassword();
                if (daoService.userIsValid(login, passwordHash)) {
                    String jwt = JWT.createJWT(login);
                    byte[] data = writeMessageIntoPacket(OBJECT_MAPPER.writeValueAsBytes(Map.of("token", jwt)));
                    writeData(data, 200, exchange);
                } else {
                    writeData("Error user not found".getBytes(), 401, exchange, false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

//        private void GetAll(HttpExchange exchange) {
//            String jwt = exchange.getRequestHeaders().getFirst("token");
//
//
//            List<Product> products;
//            try {
//                HttpClient client = HttpClient.newHttpClient();
//                HttpRequest request = HttpRequest.newBuilder()
//                        .uri(new URI("http://localhost:8765/api/product"))
//                        //                        .headers("token", jwt)
//                        .GET()
//                        .build();
//                System.out.println("here1");
//                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
//                String responseStr = response.body();
//                System.out.println("here");
//                products = Arrays.asList(OBJECT_MAPPER.readValue(responseStr, Product[].class));
//            } catch (InterruptedException | IOException | URISyntaxException e) {
//                throw new RuntimeException(e);
//            }
//            System.out.println(products);
//        }

        private void processGetAllProducts(HttpExchange exchange) {
            List<Product> products = daoService.findAllProducts();
            try {
                byte[] data = OBJECT_MAPPER.writeValueAsBytes(products);
                byte[] packet = writeMessageIntoPacket(data);
                writeData(packet, 200, exchange);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        private void processGetProductById(HttpExchange exchange) {
            String idStr = exchange.getRequestURI().getPath()
                    .replace("/api/product/", "")
                    .replace("/", "");
            int id = Integer.parseInt(idStr);
            Product product = daoService.findProductById(id);
            if (product == null) {
                writeData("Product not found".getBytes(), 404, exchange, false);
            }
            try {
                byte[] data = OBJECT_MAPPER.writeValueAsBytes(product);
                byte[] packet = writeMessageIntoPacket(data);
                writeData(packet, 200, exchange);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        private void processCreateProduct(HttpExchange exchange) {
            try {
                Product product = OBJECT_MAPPER.readValue(extractMessageFromPackage(exchange.getRequestBody()), Product.class);
                int resp = daoService.createProduct(product);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(resp != 0 ? 201 : 409, 0);
                exchange.getResponseBody().close();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }

        private void processUpdateProduct(HttpExchange exchange) {
            try {
                String idStr = exchange.getRequestURI().getPath()
                        .replace("/api/product/", "")
                        .replace("/", "");
                int id = Integer.parseInt(idStr);
                Product product = OBJECT_MAPPER.readValue(extractMessageFromPackage(exchange.getRequestBody()), Product.class);
                product.setId(id);
                int resp = daoService.updateProduct(product);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(resp != 0 ? 204 : 404, 0);
                exchange.getResponseBody().close();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }


        private void processDeleteProduct(HttpExchange exchange) {
            String idStr = exchange.getRequestURI().getPath()
                    .replace("/api/product/", "")
                    .replace("/", "");
            int id = Integer.parseInt(idStr);
            try {
                int resp = daoService.deleteProduct(id);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(resp != 0 ? 204 : 404, 0);
                exchange.getResponseBody().close();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }

        private static void writeData(byte[] data, int code, HttpExchange exchange) {
            writeData(data, code, exchange, true);
        }

        private static void writeData(byte[] data, int code, HttpExchange exchange, boolean json) {
            try {
                if (json) {
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                }
                exchange.sendResponseHeaders(code, data.length);
                OutputStream os = exchange.getResponseBody();
                os.write(data);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
