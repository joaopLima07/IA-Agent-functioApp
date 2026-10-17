package br.com.aisaas.auth.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token", indexes = @Index(name = "idx_refresh_token_family", columnList = "family_id"))
public class RefreshToken {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_id")
    private UUID replacedById;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected RefreshToken() {
    }

    public RefreshToken(long userId, String tokenHash, UUID familyId, Instant expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.familyId = familyId;
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public void revoke(Instant now, UUID replacedById) {
        this.revokedAt = now;
        this.replacedById = replacedById;
    }

    public UUID getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public UUID getReplacedById() {
        return replacedById;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
