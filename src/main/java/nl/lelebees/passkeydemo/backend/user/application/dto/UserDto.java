package nl.lelebees.passkeydemo.backend.user.application.dto;

import nl.lelebees.passkeydemo.backend.user.domain.User;

import java.util.Set;
import java.util.UUID;

public record UserDto(UUID id, String email, String displayName, Set<PasskeyDto> passkeys) {
    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getEmail().toString(), user.getDisplayName(), PasskeyDto.from(user.getPasskeys()));
    }
}
