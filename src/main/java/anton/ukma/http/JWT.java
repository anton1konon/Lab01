package anton.ukma.http;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

public class JWT {

    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public static String createJWT(String username) {
        System.out.println("in jwt");
        String ans = Jwts.builder()
                .setSubject(username)
                .signWith(key)
                .compact();
        return ans;
    }

    public static String extractUsername(String jwt) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody().getSubject();
    }

}
