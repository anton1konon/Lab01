package anton.ukma.http;

import anton.ukma.repository.DaoService;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

public class Auth extends Authenticator {

    private DaoService daoService = new DaoService();
    @Override
    public Result authenticate(HttpExchange httpExchange) {
        String jwt =  httpExchange.getRequestHeaders().getFirst("token");
        if (jwt == null) return new Failure(403);
        String login = JWT.extractUsername(jwt);
        if (!daoService.userExists(login))
            return new Failure(403);
        else
            return new Success(new HttpPrincipal(login, "USER"));
    }
}