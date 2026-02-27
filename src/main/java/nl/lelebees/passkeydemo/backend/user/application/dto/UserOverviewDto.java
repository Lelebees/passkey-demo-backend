package nl.lelebees.passkeydemo.backend.user.application.dto;

import nl.lelebees.passkeydemo.backend.user.domain.User;

import java.util.UUID;

public record UserOverviewDto(UUID id, String email, String displayName) {
    public static UserOverviewDto from(User user) {
        return new UserOverviewDto(user.getId(), user.getEmail().toString(), user.getDisplayName());
    }
}
