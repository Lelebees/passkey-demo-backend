package nl.lelebees.passkeydemo.backend.security.application.dto;

import com.webauthn4j.data.PublicKeyCredentialRequestOptions;

public record AuthenticationRequestOptionsDto(PublicKeyCredentialRequestOptions options, String session) {

}
