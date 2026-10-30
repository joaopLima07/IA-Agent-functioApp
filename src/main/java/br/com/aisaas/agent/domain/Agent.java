package br.com.aisaas.agent.domain;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String name;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private AgentStatus status = AgentStatus.DRAFT;

    @NotBlank
    private String modelProvider;

    @NotBlank
    private String modelName;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "2.0")
    private double temperature = 0.7;

    @Min(value = 0)
    @Max(value = 2048)
    private Integer maxTokens;

    @Column(columnDefinition = "TEXT")
    private String systemPrompt;

    @Enumerated(EnumType.STRING)
    private AgentVisibility visibility = AgentVisibility.PRIVATE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Version
    private Integer version;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public long getId() {
        return id;
    }

    public String getModelProvider() {
        return modelProvider;
    }

    public void setModelProvider(String modelProvider) {
        this.modelProvider = modelProvider;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public AgentVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(AgentVisibility visibility) {
        this.visibility = visibility;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
