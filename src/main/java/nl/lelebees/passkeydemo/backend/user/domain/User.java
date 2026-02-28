package nl.lelebees.passkeydemo.backend.user.domain;

import com.blueconic.browscap.Capabilities;
import com.webauthn4j.data.RegistrationData;
import jakarta.persistence.*;
import nl.lelebees.passkeydemo.backend.security.application.jwt.JwtToken;
import org.hibernate.annotations.NaturalId;

import java.util.*;

@Entity(name = "users")
public class User {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(unique = true)
    @Embedded
    @NaturalId
    private Email email;
    private String displayName;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Passkey> passkeys;

    private List<String> acceptedRefreshTokens;

    public User(Email email, String displayName) {
        this.email = email;
        this.displayName= displayName;
        this.passkeys = new HashSet<>();
        this.acceptedRefreshTokens = new ArrayList<>();
    }

    public User(Email email, Passkey passkey) {
        this.email = email;
        this.passkeys = new HashSet<>();
        passkeys.add(passkey);
        this.acceptedRefreshTokens = new ArrayList<>();
    }

    protected User() {

    }

    public UUID getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public void registerKey(Passkey passkey) {
        passkeys.add(passkey);
    }

    public void removeKey(Passkey passkey) {
        passkeys.remove(passkey);
    }

    public Set<Passkey> getPasskeys() {
        return Collections.unmodifiableSet(passkeys);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void registerKey(String userAgent, RegistrationData verifiedData, Capabilities parsedUserAgent) {
        passkeys.add(Passkey.From(this, userAgent, verifiedData, parsedUserAgent));
    }

    public void registerRefreshToken(JwtToken token) {
        if (acceptedRefreshTokens == null)
        {
            acceptedRefreshTokens = new ArrayList<>();
        }
        acceptedRefreshTokens.add(token.toString());
    }

    public List<String> getAcceptedRefreshTokens() {
        return Collections.unmodifiableList(acceptedRefreshTokens);
    }

    public void revokeRefreshTokens() {
        this.acceptedRefreshTokens = new ArrayList<>();
    }
}
