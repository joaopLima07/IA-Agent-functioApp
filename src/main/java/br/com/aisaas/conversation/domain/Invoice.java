package br.com.aisaas.conversation.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nota_fiscal")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "\\d+", message = "must contain only digits")
    @Column(name = "numero", nullable = false, length = 20)
    private String number;

    @Size(max = 5)
    @Column(name = "serie", nullable = false, length = 5)
    private String series = "1";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private InvoiceTypes types;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private InvoiceStatus status;

    @NotBlank
    @Size(max = 120)
    @Column(name = "cliente_nome", nullable = false, length = 120)
    private String clientName;

    @NotBlank
    @Size(max = 18)
    @Pattern(regexp = "[0-9A-Z]{2}\\.?[0-9A-Z]{3}\\.?[0-9A-Z]{3}/?[0-9A-Z]{4}-?[0-9]{2}", message = "invalid CNPJ format")
    @Column(name = "cliente_documento", nullable = false, length = 18)
    private String clientCNPJ;

    @Email
    @Size(max = 120)
    @Column(name = "email_destinatario", length = 120)
    private String recipientEmail;

    @NotBlank
    @Size(max = 255)
    @Column(name = "descricao", nullable = false, length = 255)
    private String description;

    @Size(max = 500)
    @Column(name = "observacao", length = 500)
    private String observation;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalValue;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_impostos", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxValue;

    @NotNull
    @Column(name = "data_emissao", nullable = false)
    private LocalDate issueDate;

    @Column(name = "data_vencimento")
    private LocalDate dueDate;

    // alteração aguardando confirmação
    @Enumerated(EnumType.STRING)
    @Column(name = "campo_pendente", length = 30)
    private InvoiceField pendingField;

    @Column(name = "valor_pendente", length = 500)
    private String pendingValue;

    @Column(name = "pendente_expira_em")
    private LocalDateTime pendingExpiresAt;

    // última alteração confirmada
    @Enumerated(EnumType.STRING)
    @Column(name = "ultima_alteracao_campo", length = 30)
    private InvoiceField lastChangeField;

    @Column(name = "ultima_alteracao_valor_anterior", length = 500)
    private String lastChangePreviousValue;

    @Column(name = "ultima_alteracao_em")
    private LocalDateTime lastChangeAt;

    @Column(name = "ultima_alteracao_user_id")
    private Long lastChangeUserId;

    @Column(name = "ultima_alteracao_agent_id")
    private Long lastChangeAgentId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_icms", nullable = false, precision = 12, scale = 2)
    private BigDecimal icmsValue = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_ipi", nullable = false, precision = 12, scale = 2)
    private BigDecimal ipiValue = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_pis", nullable = false, precision = 12, scale = 2)
    private BigDecimal pisValue = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_cofins", nullable = false, precision = 12, scale = 2)
    private BigDecimal cofinsValue = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_iss", nullable = false, precision = 12, scale = 2)
    private BigDecimal issValue = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_irrf", nullable = false, precision = 12, scale = 2)
    private BigDecimal irrfValue = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_csll", nullable = false, precision = 12, scale = 2)
    private BigDecimal csllValue = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_inss", nullable = false, precision = 12, scale = 2)
    private BigDecimal inssValue = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_ibs", nullable = false, precision = 12, scale = 2)
    private BigDecimal ibsValue = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_cbs", nullable = false, precision = 12, scale = 2)
    private BigDecimal cbsValue = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "valor_is", nullable = false, precision = 12, scale = 2)
    private BigDecimal selectiveTaxValue = BigDecimal.ZERO;

    public void recalculateTaxValue() {
        this.taxValue = icmsValue.add(ipiValue).add(pisValue).add(cofinsValue).add(issValue)
                .add(irrfValue).add(csllValue).add(inssValue)
                .add(ibsValue).add(cbsValue).add(selectiveTaxValue);
    }

    protected Invoice() {
    }

    public Invoice(Long userId, String number, String series, InvoiceTypes types, InvoiceStatus status,
                   String clientName, String clientCNPJ, String recipientEmail, String description,
                   String observation, BigDecimal totalValue, BigDecimal taxValue,
                   LocalDate issueDate, LocalDate dueDate,
                   BigDecimal icmsValue, BigDecimal ipiValue, BigDecimal pisValue, BigDecimal cofinsValue,
                   BigDecimal issValue, BigDecimal irrfValue, BigDecimal csllValue, BigDecimal inssValue,
                   BigDecimal ibsValue, BigDecimal cbsValue, BigDecimal selectiveTaxValue) {
        this.userId = userId;
        this.number = number;
        this.series = (series == null || series.isBlank()) ? "1" : series;
        this.types = types;
        this.status = status;
        this.clientName = clientName;
        this.clientCNPJ = clientCNPJ;
        this.recipientEmail = recipientEmail;
        this.description = description;
        this.observation = observation;
        this.totalValue = totalValue;
        this.taxValue = taxValue;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.icmsValue = valueOrZero(icmsValue);
        this.ipiValue = valueOrZero(ipiValue);
        this.pisValue = valueOrZero(pisValue);
        this.cofinsValue = valueOrZero(cofinsValue);
        this.issValue = valueOrZero(issValue);
        this.irrfValue = valueOrZero(irrfValue);
        this.csllValue = valueOrZero(csllValue);
        this.inssValue = valueOrZero(inssValue);
        this.ibsValue = valueOrZero(ibsValue);
        this.cbsValue = valueOrZero(cbsValue);
        this.selectiveTaxValue = valueOrZero(selectiveTaxValue);
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    // ---------- regras da alteração com confirmação ----------

    public void proposeChange(InvoiceField field, String newValue, LocalDateTime expiresAt) {
        if (status == InvoiceStatus.CANCELADA) {
            throw new IllegalStateException("Nota cancelada não pode ser alterada");
        }
        this.pendingField = field;
        this.pendingValue = newValue;
        this.pendingExpiresAt = expiresAt;
    }

    public boolean hasValidPendingChange(LocalDateTime now) {
        return pendingField != null && pendingExpiresAt != null && pendingExpiresAt.isAfter(now);
    }

    public void confirmChange(Long confirmedByUserId, Long agentId, LocalDateTime now) {
        if (!hasValidPendingChange(now)) {
            throw new IllegalStateException("Não há alteração pendente válida para esta nota");
        }
        InvoiceField field = this.pendingField;
        this.lastChangePreviousValue = currentValueOf(field);
        applyValue(field, this.pendingValue);
        this.lastChangeField = field;
        this.lastChangeAt = now;
        this.lastChangeUserId = confirmedByUserId;
        this.lastChangeAgentId = agentId;
        cancelPendingChange();
    }

    public void cancelPendingChange() {
        this.pendingField = null;
        this.pendingValue = null;
        this.pendingExpiresAt = null;
    }

    private String currentValueOf(InvoiceField field) {
        return switch (field) {
            case DESCRICAO -> description;
            case EMAIL_DESTINATARIO -> recipientEmail;
            case OBSERVACAO -> observation;
        };
    }

    private void applyValue(InvoiceField field, String value) {
        switch (field) {
            case DESCRICAO -> this.description = value;
            case EMAIL_DESTINATARIO -> this.recipientEmail = value;
            case OBSERVACAO -> this.observation = value;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public InvoiceTypes getTypes() {
        return types;
    }

    public void setTypes(InvoiceTypes types) {
        this.types = types;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientCNPJ() {
        return clientCNPJ;
    }

    public void setClientCNPJ(String clientCNPJ) {
        this.clientCNPJ = clientCNPJ;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getTaxValue() {
        return taxValue;
    }

    public void setTaxValue(BigDecimal taxValue) {
        this.taxValue = taxValue;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public InvoiceField getPendingField() {
        return pendingField;
    }

    public void setPendingField(InvoiceField pendingField) {
        this.pendingField = pendingField;
    }

    public String getPendingValue() {
        return pendingValue;
    }

    public void setPendingValue(String pendingValue) {
        this.pendingValue = pendingValue;
    }

    public LocalDateTime getPendingExpiresAt() {
        return pendingExpiresAt;
    }

    public void setPendingExpiresAt(LocalDateTime pendingExpiresAt) {
        this.pendingExpiresAt = pendingExpiresAt;
    }

    public InvoiceField getLastChangeField() {
        return lastChangeField;
    }

    public void setLastChangeField(InvoiceField lastChangeField) {
        this.lastChangeField = lastChangeField;
    }

    public String getLastChangePreviousValue() {
        return lastChangePreviousValue;
    }

    public void setLastChangePreviousValue(String lastChangePreviousValue) {
        this.lastChangePreviousValue = lastChangePreviousValue;
    }

    public LocalDateTime getLastChangeAt() {
        return lastChangeAt;
    }

    public void setLastChangeAt(LocalDateTime lastChangeAt) {
        this.lastChangeAt = lastChangeAt;
    }

    public Long getLastChangeUserId() {
        return lastChangeUserId;
    }

    public void setLastChangeUserId(Long lastChangeUserId) {
        this.lastChangeUserId = lastChangeUserId;
    }

    public Long getLastChangeAgentId() {
        return lastChangeAgentId;
    }

    public void setLastChangeAgentId(Long lastChangeAgentId) {
        this.lastChangeAgentId = lastChangeAgentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}