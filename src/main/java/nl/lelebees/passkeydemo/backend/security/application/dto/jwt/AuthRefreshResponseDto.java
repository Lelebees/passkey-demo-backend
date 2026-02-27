package nl.lelebees.passkeydemo.backend.security.application.dto.jwt;

import nl.lelebees.passkeydemo.backend.security.application.jwt.JwtToken;

public record AuthRefreshResponseDto(JwtToken accessToken) {
}
