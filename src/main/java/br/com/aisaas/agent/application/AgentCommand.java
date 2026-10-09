package br.com.aisaas.agent.application;

import br.com.aisaas.agent.domain.AgentStatus;
import br.com.aisaas.agent.domain.AgentVisibility;
import java.time.LocalDateTime;

public class AgentCommand {
    private long id;

    private String name;

    private String description;

    private AgentStatus status = AgentStatus.DRAFT;

    private String modelProvider;

    private String modelName;

    private double temperature = 0.7;

    private Integer maxTokens;

    private String systemPrompt;

    private AgentVisibility visibility = AgentVisibility.PRIVATE;

    private LocalDateTime createdAt;

    private Integer version;

    private LocalDateTime updatedAt;

    public AgentCommand(long id, String name, String description, AgentStatus status, String modelProvider, String modelName, double temperatura, Integer maxTokens, String systemPrompt, AgentVisibility visibility, LocalDateTime createdAt, Integer version, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.modelProvider = modelProvider;
        this.modelName = modelName;
        this.temperature = temperatura;
        this.maxTokens = maxTokens;
        this.systemPrompt = systemPrompt;
        this.visibility = visibility;
        this.createdAt = createdAt;
        this.version = version;
        this.updatedAt = updatedAt;
    }

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
