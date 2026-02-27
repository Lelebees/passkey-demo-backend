package nl.lelebees.passkeydemo.backend.user.domain;

import com.blueconic.browscap.Capabilities;
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
    @Column(name = "created_by_user_agent")
    private String userAgent;
    @Column(name = "created_by_browser")
    private String browser;
    @Column(name = "created_by_platform")
    private String platform;
    @Convert(converter = CredentialRecordConverter.class)
    private CredentialRecord credentialRecord;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    public Passkey(User owner, byte[] id, LocalDateTime createdAt, String userAgent, RegistrationData data, Capabilities parsedUserAgent) {
        this.id = id;
        this.createdAt = createdAt;
        this.userAgent = userAgent;
        this.credentialRecord = new CredentialRecordImpl(data.getAttestationObject(), data.getCollectedClientData(), data.getClientExtensions(), data.getTransports());
        this.owner = owner;
        this.browser = parsedUserAgent.getBrowser();
        this.platform = parsedUserAgent.getPlatform();
    }

    protected Passkey() {

    }

    public static Passkey From(User user, String createdByUserAgent, RegistrationData verifiedData, Capabilities parsedUserAgent) {
        return new Passkey(user, verifiedData.getAttestationObject().getAuthenticatorData().getAttestedCredentialData().getCredentialId(), LocalDateTime.now(), createdByUserAgent, verifiedData, parsedUserAgent);
    }

    public byte[] getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getUserAgent() {
        return userAgent;
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

    public String getCreatedByBrowser() {
        return browser;
    }

    public String getCreatedByPlatform() {
        return platform;
    }
}
