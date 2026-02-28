package nl.lelebees.passkeydemo.backend.security.application.jwt;

import com.fasterxml.jackson.annotation.JsonValue;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;

public record JwtToken(String token) {

    private static final Logger logger = LoggerFactory.getLogger(JwtToken.class);

    public static JwtToken fromUsername(String username, SecretKey signingKey, long jwtExpirationMs) {
        return new JwtToken(Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(signingKey)
                .compact());
    }

    public String extractUsername(SecretKey signingKey){
        return Jwts.parser().decryptWith(signingKey).verifyWith(signingKey).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean isValid(SecretKey signingKey) {
        try {
            logger.debug(token);
            Jwts.parser().decryptWith(signingKey).verifyWith(signingKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            logger.error("Caught exception in key decryption: ", e);
            return false;
        }
    }
    @JsonValue
    @Override
    public String toString() {
        return token;
    }
}
