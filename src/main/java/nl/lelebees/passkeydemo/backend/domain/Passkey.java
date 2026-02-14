package nl.lelebees.passkeydemo.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Passkey {
    @Id
    String id;
    LocalDateTime createdAt;
    String createdByUserAgent;
    String description;

    public Passkey(String id, LocalDateTime createdAt, String createdByUserAgent, String description) {
        this.id = id;
        this.createdAt = createdAt;
        this.createdByUserAgent = createdByUserAgent;
        this.description = description;
    }

    protected Passkey() {

    }
}
