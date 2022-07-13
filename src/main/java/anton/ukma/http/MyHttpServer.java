package anton.ukma.http;

import anton.ukma.model.Product;
import anton.ukma.repository.DaoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MyHttpServer extends Thread {

    private static final DaoService daoService = new DaoService();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
                new EndpointHandler("/api/product/?", "GET", this::processGetAll),
                new EndpointHandler("/product/?", "GET", this::GetAll),

                new EndpointHandler("/api/product/?", "PUT", this::processCreateProduct),
                new EndpointHandler("/product/?", "PUT", this::processCreateProduct),

                new EndpointHandler("/api/product/(\\d+)", "GET", this::processGetById),
                new EndpointHandler("/product/(\\d+)", "GET", this::processGetById),

                new EndpointHandler("/api/product/(\\d+)", "POST", this::processUpdateProduct),
                new EndpointHandler("/product/(\\d+)", "POST", this::processUpdateProduct),

                new EndpointHandler("/api/product/(\\d+)", "DELETE", this::processDeleteProduct),
                new EndpointHandler("/product/(\\d+)", "DELETE", this::processDeleteProduct),

                new EndpointHandler("/login", "POST", this::processLogin)
        );

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
            try {
                User user = OBJECT_MAPPER.readValue(stream, User.class);
                String login = user.getLogin();
                String passwordHash = user.getPassword();
                if (daoService.userIsValid(login, passwordHash)) {
                    String jwt = JWT.createJWT(login);
                    byte[] data = OBJECT_MAPPER.writeValueAsBytes(Map.of("token", jwt));
                    writeData(data, 200, exchange);
                } else {
                    writeData("Error user not found".getBytes(), 401, exchange, false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void GetAll(HttpExchange exchange) {

        }

        private void processGetAll(HttpExchange exchange) {
            List<Product> products = daoService.findAllProducts();
            try {
                byte[] data = OBJECT_MAPPER.writeValueAsBytes(products);
                writeData(data, 200, exchange);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        private void processGetById(HttpExchange exchange) {
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
                writeData(data, 200, exchange);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        private void processCreateProduct(HttpExchange exchange) {
            try {
                Product product = OBJECT_MAPPER.readValue(exchange.getRequestBody(), Product.class);
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
                Product product = OBJECT_MAPPER.readValue(exchange.getRequestBody(), Product.class);
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
