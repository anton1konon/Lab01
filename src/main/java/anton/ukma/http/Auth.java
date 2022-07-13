package anton.ukma.http;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

public class Auth extends Authenticator {
    @Override
    public Result authenticate(HttpExchange httpExchange) {
        System.out.println("in auth");
        String jwt =  httpExchange.getRequestHeaders().getFirst("token");
        if (jwt == null) return new Failure(403);
        String login = JWT.extractUsername(jwt);
        if (!login.equals("user"))
            return new Failure(403);
        else
            return new Success(new HttpPrincipal(login, "USER"));
    }
}