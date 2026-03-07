package nl.lelebees.passkeydemo.backend.security.application.dto;

import com.webauthn4j.data.PublicKeyCredentialHints;

public record DesiredMediumDto(PublicKeyCredentialHints preferredMedium) {
}
