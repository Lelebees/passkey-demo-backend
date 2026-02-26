package nl.lelebees.passkeydemo.backend.application.dto;

import com.webauthn4j.data.PublicKeyCredentialRequestOptions;

public record AuthenticationRequestOptionsDto(PublicKeyCredentialRequestOptions options, String session) {

}
