package nl.lelebees.passkeydemo.backend.security.application.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import nl.lelebees.passkeydemo.backend.user.application.UserService;
import nl.lelebees.passkeydemo.backend.user.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.user.domain.IncorrectEmailFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtils {

    private final String accessTokenSecret;
    private final long accessTokenExpirationMs;
    private final String refreshTokenSecret;
    private final long refreshTokenExpirationMs;
    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    public JwtUtils(@Value("${jwt.access.secret}") String accessTokenSecret, @Value("${jwt.access.expiration}") long accessTokenExpirationMs, @Value("${jwt.refresh.secret}") String refreshTokenSecret, @Value("${jwt.refresh.expiration}") long refreshTokenExpirationMs, UserService userService) {
        this.accessTokenSecret = accessTokenSecret;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenSecret = refreshTokenSecret;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.userService = userService;
    }

    private SecretKey getSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtToken generateAccessToken(String username) {
        return JwtToken.fromUsername(username, getSigningKey(accessTokenSecret), accessTokenExpirationMs);
    }

    public JwtToken generateAccessToken(JwtToken refreshToken) {
        return JwtToken.fromUsername(refreshToken.extractUsername(getSigningKey(refreshTokenSecret)), getSigningKey(accessTokenSecret), accessTokenExpirationMs);
    }

    public JwtToken generateRefreshToken(String username) throws IncorrectEmailFormatException, UserNotFoundException {
        JwtToken token = JwtToken.fromUsername(username, getSigningKey(refreshTokenSecret), refreshTokenExpirationMs);
        try {
            userService.registerRefreshToken(username, token);
        } catch (IncorrectEmailFormatException e) {
            logger.error("Attempted to mint refresh token with invalid username.", e);
            throw e;
        } catch (UserNotFoundException e) {
            logger.error("Attempted to mint refresh token for user that does not exist.", e);
            throw e;
        }
        return token;
    }

    public String extractUsername(String token) {
        return Jwts.parser().decryptWith(getSigningKey(accessTokenSecret)).verifyWith(getSigningKey(accessTokenSecret)).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public String extractUsername(JwtToken token) {
        try {
            return token.extractUsername(getSigningKey(accessTokenSecret));
        } catch (Exception e) {
            return token.extractUsername(getSigningKey(refreshTokenSecret));
        }
    }

    public boolean isValid (JwtToken token) {
        if (token.isValid(getSigningKey(accessTokenSecret))) {
            return true;
        }
        return token.isValid(getSigningKey(refreshTokenSecret));
    }

    public boolean isValidRefreshToken(JwtToken token) {
        return token.isValid(getSigningKey(refreshTokenSecret));
    }

    public boolean isValidAccessToken(JwtToken token) {
        return token.isValid(getSigningKey(accessTokenSecret));
    }
}
