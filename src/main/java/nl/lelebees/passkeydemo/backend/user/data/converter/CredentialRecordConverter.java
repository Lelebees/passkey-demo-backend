package nl.lelebees.passkeydemo.backend.user.data.converter;

import com.webauthn4j.converter.AttestedCredentialDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.statement.AttestationStatement;
import com.webauthn4j.data.extension.authenticator.AuthenticationExtensionsAuthenticatorOutputs;
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientOutput;
import com.webauthn4j.util.Base64UrlUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;

import java.lang.reflect.Type;
import java.util.Set;
@Converter
public class CredentialRecordConverter implements AttributeConverter<CredentialRecord, String> {
    private final ObjectConverter objectConverter = new ObjectConverter();
    private final AttestedCredentialDataConverter attestedCredentialDataConverter = new AttestedCredentialDataConverter(objectConverter);

    @Override
    public String convertToDatabaseColumn(CredentialRecord credentialRecord) {
        byte[] serializedAttestationData = attestedCredentialDataConverter.convert(credentialRecord.getAttestedCredentialData());
        String attestationData = Base64UrlUtil.encodeToString(serializedAttestationData);

        AttestationStatementEnvelope envelope = new AttestationStatementEnvelope(credentialRecord.getAttestationStatement());
        byte[] serializedEnvelope = objectConverter.getCborMapper().writeValueAsBytes(envelope);
        String envelopeData = Base64UrlUtil.encodeToString(serializedEnvelope);

        String serializedTransports = objectConverter.getJsonMapper().writeValueAsString(credentialRecord.getTransports());

        String counter = String.valueOf(credentialRecord.getCounter());

        byte[] serializedAuthenticatorExtensions = objectConverter.getCborMapper().writeValueAsBytes(credentialRecord.getAuthenticatorExtensions());
        String authenticatorExtensions = Base64UrlUtil.encodeToString(serializedAuthenticatorExtensions);


        String serializedClientExtensions = objectConverter.getJsonMapper().writeValueAsString(credentialRecord.getClientExtensions());

        return "%s|%s|%s|%s|%s|%s".formatted(attestationData, envelopeData, serializedTransports, counter, authenticatorExtensions, serializedClientExtensions);
    }

    @Override
    public CredentialRecord convertToEntityAttribute(String s) {
        String[] serialized = s.split("\\|");

        AttestedCredentialData deserializedAttestedCredentialData = attestedCredentialDataConverter.convert(Base64UrlUtil.decode(serialized[0]));

        AttestationStatementEnvelope deserializedEnvelope = objectConverter.getCborMapper().readValue(Base64UrlUtil.decode(serialized[1]), AttestationStatementEnvelope.class);
        AttestationStatement deserializedAttestationStatement = deserializedEnvelope.getAttestationStatement();

        Set<AuthenticatorTransport> transports = objectConverter.getJsonMapper().readValue(serialized[2], new TypeReference<>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });
        long counter = Long.decode(serialized[3]);

        AuthenticationExtensionsAuthenticatorOutputs<RegistrationExtensionAuthenticatorOutput> authenticatorExtensions = objectConverter.getCborMapper().readValue(Base64UrlUtil.decode(serialized[4]), new TypeReference<>() {});
        AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput> clientExtensions = objectConverter.getJsonMapper().readValue(serialized[5], new TypeReference<>() {});
        return new CredentialRecordImpl(
                deserializedAttestationStatement,
                null, null, null,
                counter,
                deserializedAttestedCredentialData,
                authenticatorExtensions,
                null,
                clientExtensions,
                transports
        );
    }
}
