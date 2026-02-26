package nl.lelebees.passkeydemo.backend.user.domain;

import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.RegistrationData;
import jakarta.persistence.*;
import nl.lelebees.passkeydemo.backend.user.data.converter.CredentialRecordConverter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Passkey {
    @Id
    private byte[] id;
    private LocalDateTime createdAt;
    private String createdByUserAgent;
    @Convert(converter = CredentialRecordConverter.class)
    private CredentialRecord credentialRecord;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    public Passkey(User owner, byte[] id, LocalDateTime createdAt, String createdByUserAgent, RegistrationData data) {
        this.id = id;
        this.createdAt = createdAt;
        this.createdByUserAgent = createdByUserAgent;
        this.credentialRecord = new CredentialRecordImpl(data.getAttestationObject(), data.getCollectedClientData(), data.getClientExtensions(), data.getTransports());
        this.owner = owner;
    }

    protected Passkey() {

    }

    public static Passkey From(User user, String createdByUserAgent, RegistrationData verifiedData) {
        return new Passkey(user, verifiedData.getAttestationObject().getAuthenticatorData().getAttestedCredentialData().getCredentialId(), LocalDateTime.now(), createdByUserAgent, verifiedData);
    }

    public byte[] getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedByUserAgent() {
        return createdByUserAgent;
    }

    public CredentialRecord getCredentialRecord() {
        return credentialRecord;
    }

    public UUID getOwnerId(){
        return owner.getId();
    }

    public Email getOwnerEmail() {
        return owner.getEmail();
    }
}
