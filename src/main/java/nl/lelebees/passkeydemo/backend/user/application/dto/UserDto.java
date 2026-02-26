package nl.lelebees.passkeydemo.backend.user.application.dto;

import nl.lelebees.passkeydemo.backend.user.domain.User;

import java.util.UUID;

public record UserDto(UUID id, String email, String displayName) {
    public static UserDto From(User user) {
        return new UserDto(user.getId(), user.getEmail().toString(), user.getDisplayName());
    }
}
