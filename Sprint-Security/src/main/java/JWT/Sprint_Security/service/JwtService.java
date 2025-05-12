package JWT.Sprint_Security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;

@Service // Marks this class as a Spring-managed service bean
public class JwtService {

    // Inject the secret key from application.properties (used for signing JWTs)
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    // Inject the token expiration duration (in milliseconds) from application.properties
    @Value("${security.jwt.expiration-time}")
    private Long jwtExpiration;

    // Extract the username (the "subject" claim) from a given JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); // Use helper method to get the subject claim
    }

    // Generic method to extract any claim from the token using a lambda function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // First parse all claims from the token
        return claimsResolver.apply(claims); // Then apply the resolver to extract the desired claim
    }

    // Generate a JWT token using only user details (with no extra claims)
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails); // Call the overloaded method with an empty claim map
    }

    // Generate a JWT token with additional claims provided by the caller
    public String generateToken(Map<String, Object> extractClaims, UserDetails userDetails) {
        return buildToken(extractClaims, userDetails, jwtExpiration); // Delegate to private method
    }

    // Expose expiration time for use in other services (optional convenience)
    public long getExpirationTime() {
        return jwtExpiration;
    }

    // Constructs and signs the JWT token with claims, user data, and expiration
    private String buildToken(Map<String, Object> extractClaims, UserDetails userDetails, long expirationTime) {
        return Jwts.builder()
                .setClaims(extractClaims) // Set custom claims (optional key-value data)
                .setSubject(userDetails.getUsername()) // Set the subject (usually the username or email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Set the issued time (current time)
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Set expiration time
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Sign the token with the secret key using HMAC SHA-256
                .compact(); // Convert the JWT object to a compact string format (the actual token)
    }

    // Validates whether the token is valid for the given user (not expired and username matches)
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token); // Extract username from token
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)); // Check username match and expiration
    }

    // Check whether the token has expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date()); // If expiration date is before now, it's expired
    }

    // Extracts the expiration date from the token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration); // Use the generic extractor
    }

    // Parse the JWT token and extract all claims (like subject, expiration, etc.)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey()) // Set signing key to validate the token's signature
                .build()
                .parseClaimsJws(token) // Parse and validate the token (throws if invalid)
                .getBody(); // Extract and return the payload (claims)
    }

    // Convert the Base64-encoded secret key to a `Key` object used for signing
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); // Decode from base64 to raw bytes
        return Keys.hmacShaKeyFor(keyBytes); // Generate HMAC-SHA key for signing the JWT
    }
}
