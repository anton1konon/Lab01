package anton.ukma.http;

import com.sun.net.httpserver.HttpExchange;

import java.util.function.Consumer;

public class EndpointHandler {
    private final String pathPatter, httpMethod;
    private final Consumer<HttpExchange> handler;

    public EndpointHandler(String pathPatter, String httpMethod, Consumer<HttpExchange> handler) {
        this.pathPatter = pathPatter;
        this.httpMethod = httpMethod;
        this.handler = handler;
    }

    public boolean isMatch(HttpExchange exchange) {
        System.out.println(this.httpMethod + "  " + this.pathPatter);
        if (!exchange.getRequestMethod().equals(httpMethod)) return false;
        return exchange.getRequestURI().getPath().matches(pathPatter);
    }

    public void handle(HttpExchange exchange) {
        handler.accept(exchange);
    }

}