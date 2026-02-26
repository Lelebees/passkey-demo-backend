package nl.lelebees.passkeydemo.backend.user.domain;

import com.webauthn4j.data.RegistrationData;
import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    public User(Email email, String displayName) {
        this.email = email;
        this.displayName= displayName;
        this.passkeys = new HashSet<>();
    }

    public User(Email email, Passkey passkey) {
        this.email = email;
        this.passkeys = new HashSet<>();
        passkeys.add(passkey);
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

    public void registerKey(String userAgent, RegistrationData verifiedData) {
        passkeys.add(Passkey.From(this, userAgent, verifiedData));

    }
}
