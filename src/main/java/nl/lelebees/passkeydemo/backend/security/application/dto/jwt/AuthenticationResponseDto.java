package nl.lelebees.passkeydemo.backend.security.application.dto.jwt;

import nl.lelebees.passkeydemo.backend.security.application.jwt.JwtToken;
import nl.lelebees.passkeydemo.backend.security.application.jwt.JwtUtils;
import nl.lelebees.passkeydemo.backend.user.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.user.domain.IncorrectEmailFormatException;

public record AuthenticationResponseDto(JwtToken accessToken, JwtToken refreshToken) {

    public static AuthenticationResponseDto from(JwtUtils jwtUtils, String username) throws UserNotFoundException, IncorrectEmailFormatException {
        return new AuthenticationResponseDto(jwtUtils.generateAccessToken(username), jwtUtils.generateRefreshToken(username));
    }
}
