package nl.lelebees.passkeydemo.backend.user.application.dto;

import com.webauthn4j.util.Base64UrlUtil;
import nl.lelebees.passkeydemo.backend.user.domain.Passkey;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record PasskeyDto(String id, String browser, String operatingSystem, LocalDateTime createdAt) {
    public static PasskeyDto from(Passkey passkey) {
        return new PasskeyDto(Base64UrlUtil.encodeToString(passkey.getId()), passkey.getCreatedByBrowser(), passkey.getCreatedByPlatform(), passkey.getCreatedAt());
    }

    public static Set<PasskeyDto> from(Set<Passkey> collection) {
        return collection.stream().map(PasskeyDto::from).collect(Collectors.toSet());
    }
}
