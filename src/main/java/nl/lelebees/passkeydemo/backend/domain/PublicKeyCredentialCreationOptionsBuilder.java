package nl.lelebees.passkeydemo.backend.domain;

import com.webauthn4j.data.*;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientInputs;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientInput;

import java.util.Collections;
import java.util.List;

public class PublicKeyCredentialCreationOptionsBuilder {

    private final PublicKeyCredentialRpEntity rp;
    private final PublicKeyCredentialUserEntity user;

    private final Challenge challenge;
    private final List<PublicKeyCredentialParameters> pubKeyCredParams;
    private Long timeout;
    private List<PublicKeyCredentialDescriptor> excludeCredentials;
    private AuthenticatorSelectionCriteria authenticatorSelection;
    private List<PublicKeyCredentialHints> hints;
    private AttestationConveyancePreference attestation;
    private List<String> attestationFormats;
    private AuthenticationExtensionsClientInputs<RegistrationExtensionClientInput> extensions;


    public PublicKeyCredentialCreationOptionsBuilder(PublicKeyCredentialRpEntity rp, PublicKeyCredentialUserEntity user, Challenge challenge, List<PublicKeyCredentialParameters> pubKeyCredParams) {
        this.rp = rp;
        this.user = user;
        this.challenge = challenge;
        this.pubKeyCredParams = pubKeyCredParams;
        this.excludeCredentials = Collections.emptyList();
    }

    public PublicKeyCredentialCreationOptions build() {
        return new PublicKeyCredentialCreationOptions(rp, user, challenge, pubKeyCredParams, timeout, excludeCredentials, authenticatorSelection, hints, attestation, attestationFormats, extensions);
    }

    public PublicKeyCredentialCreationOptionsBuilder timeout(Long timeout) {
        this.timeout = timeout;
        return this;
    }

    public PublicKeyCredentialCreationOptionsBuilder excludeCredentials(List<PublicKeyCredentialDescriptor> excludeCredentials) {
        this.excludeCredentials = excludeCredentials;
        return this;
    }

    public PublicKeyCredentialCreationOptionsBuilder authenticatorSelection(AuthenticatorSelectionCriteria authenticatorSelection) {
        this.authenticatorSelection = authenticatorSelection;
        return this;
    }

    public PublicKeyCredentialCreationOptionsBuilder hints(List<PublicKeyCredentialHints> hints) {
        this.hints = hints;
        return this;
    }

    public PublicKeyCredentialCreationOptionsBuilder attestation(AttestationConveyancePreference attestation) {
        this.attestation = attestation;
        return this;
    }

    public PublicKeyCredentialCreationOptionsBuilder attestationFormats(List<String> attestationFormats) {
        this.attestationFormats = attestationFormats;
        return this;
    }

    public PublicKeyCredentialCreationOptionsBuilder extensions(AuthenticationExtensionsClientInputs<RegistrationExtensionClientInput> extensions) {
        this.extensions = extensions;
        return this;
    }
}
