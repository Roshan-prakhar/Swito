package in.roshan.foodiesapi.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    @Value("${jwt.secret.key}")
    private String SECRET_KEY;
    
  
    public JwtUtil() {
        // This will be called after dependency injection
    }
    
    public void logSecretKey() {
        System.out.println("JWT Util - Secret key loaded: " + (SECRET_KEY != null ? "YES (length: " + SECRET_KEY.length() + ")" : "NO"));
        if (SECRET_KEY != null) {
            System.out.println("JWT Util - Secret key first 10 chars: " + SECRET_KEY.substring(0, Math.min(10, SECRET_KEY.length())));
        }
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claiams = new HashMap<>();
        return createToken(claiams, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claiams, String subject) {
        return Jwts.builder()
                .setClaims(claiams)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) //10 hours expiration
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            System.out.println("JWT Util - Token username: " + username);
            System.out.println("JWT Util - User details username: " + userDetails.getUsername());
            System.out.println("JWT Util - Username match: " + username.equals(userDetails.getUsername()));
            
            boolean expired = isTokenExpired(token);
            System.out.println("JWT Util - Token expired: " + expired);
            
            boolean result = username.equals(userDetails.getUsername()) && !expired;
            System.out.println("JWT Util - Final validation result: " + result);
            
            return result;
        } catch (Exception e) {
            System.out.println("JWT Util - Validation exception: " + e.getMessage());
            return false;
        }
    }

}
