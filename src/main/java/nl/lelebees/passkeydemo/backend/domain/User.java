package nl.lelebees.passkeydemo.backend.domain;

import jakarta.persistence.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class User {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(unique = true)
    private String email;
    @OneToMany
    private Set<Passkey> passkeys;

    public User(String email) {
        this.email = email;
        this.passkeys = new HashSet<>();
    }

    protected User() {

    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
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
}
