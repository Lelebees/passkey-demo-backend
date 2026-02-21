package nl.lelebees.passkeydemo.backend.application.dto;

import nl.lelebees.passkeydemo.backend.domain.ChallengeEntity;
import nl.lelebees.passkeydemo.backend.domain.User;

import java.util.UUID;

public record UserDto(UUID id, String email, String displayName, ChallengeEntity issuedChallenge) {
    public static UserDto From(User user) {
        return new UserDto(user.getId(), user.getEmail().toString(), user.getDisplayName(), user.getIssuedChallenge());
    }
}
