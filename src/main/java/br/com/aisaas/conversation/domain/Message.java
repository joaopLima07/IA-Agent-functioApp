package br.com.aisaas.conversation.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private long conversationId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MessageRole role;

    private Long agentId;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String content;

    private int inputToken;

    private int outputToken;

    private int latencyMs;

    private int totalToken;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public int getTotalToken() {
        return totalToken;
    }

    public void setTotalToken(int totalToken) {
        this.totalToken = totalToken;
    }

    public int getInputToken() {
        return inputToken;
    }

    public void setInputToken(int inputToken) {
        this.inputToken = inputToken;
    }

    public int getOutputToken() {
        return outputToken;
    }

    public void setOutputToken(int outputToken) {
        this.outputToken = outputToken;
    }

    public int getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(int latencyMs) {
        this.latencyMs = latencyMs;
    }

    public long getId() {
        return id;
    }

    public long getConversationId() {
        return conversationId;
    }

    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }

    public MessageRole getRole() {
        return role;
    }

    public void setRole(MessageRole role) {
        this.role = role;
    }

    public Long getAgentId() {
        return agentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
