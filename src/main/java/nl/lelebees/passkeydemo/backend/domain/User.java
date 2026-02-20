package nl.lelebees.passkeydemo.backend.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;

import java.util.*;

@Entity
public class User {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(unique = true)
    @Embedded
    @NaturalId
    private Email email;
    private String displayName;
    @OneToMany
    private Set<Passkey> passkeys;
    @OneToMany
    private Set<ChallengeEntity> issuedChallenges;

    public User(Email email, String displayName, ChallengeEntity challenge) {
        this.email = email;
        this.displayName= displayName;
        this.passkeys = new HashSet<>();
        this.issuedChallenges = new HashSet<>(List.of(challenge));
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
}
