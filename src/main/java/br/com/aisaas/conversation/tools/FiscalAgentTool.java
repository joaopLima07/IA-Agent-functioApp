package br.com.aisaas.conversation.tools;

import br.com.aisaas.conversation.domain.Invoice;
import br.com.aisaas.conversation.domain.InvoiceStatus;
import br.com.aisaas.conversation.domain.InvoiceTypes;
import br.com.aisaas.conversation.domain.exceptions.*;
import br.com.aisaas.conversation.infra.FiscalAgentRepository;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.networknt.schema.OutputFormat;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.util.MethodInvocationUtils;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static br.com.aisaas.conversation.domain.InvoiceStatus.CANCELADA;

@Component
public class FiscalAgentTool {

    private final FiscalAgentRepository fiscalAgentRepository;

    public FiscalAgentTool(FiscalAgentRepository fiscalAgentRepository) {
        this.fiscalAgentRepository = fiscalAgentRepository;
    }

    @JsonClassDescription("Valores de cada imposto da nota fiscal, em reais. Preencha apenas os impostos que aparecem no documento.")
    public record FiscalTax(
        @JsonPropertyDescription("ICMS (imposto estadual sobre circulação de mercadorias), em reais. Comum em notas de PRODUTO. Ex.: 432.00")
        @PositiveOrZero
        BigDecimal icms,

        @JsonPropertyDescription("IPI (imposto federal sobre produtos industrializados), em reais. Comum em notas de PRODUTO")
        @PositiveOrZero
        BigDecimal ipi,

        @JsonPropertyDescription("PIS (contribuição federal), em reais")
        @PositiveOrZero
        BigDecimal pis,

        @JsonPropertyDescription("COFINS (contribuição federal), em reais")
        @PositiveOrZero
        BigDecimal cofins,

        @JsonPropertyDescription("ISS (imposto municipal sobre serviços), em reais. Comum em notas de SERVICO. Ex.: 765.00")
        @PositiveOrZero
        BigDecimal iss,

        @JsonPropertyDescription("IRRF (imposto de renda retido na fonte), em reais. Comum em notas de SERVICO")
        @PositiveOrZero
        BigDecimal irrf,

        @JsonPropertyDescription("CSLL (contribuição social sobre o lucro líquido), em reais. Comum em notas de SERVICO")
        @PositiveOrZero
        BigDecimal csll,

        @JsonPropertyDescription("INSS retido (contribuição previdenciária), em reais. Comum em notas de SERVICO")
        @PositiveOrZero
        BigDecimal inss,

        @JsonPropertyDescription("IBS (imposto sobre bens e serviços da reforma tributária), em reais. Informe só se constar no documento")
        @PositiveOrZero
        BigDecimal ibs,

        @JsonPropertyDescription("CBS (contribuição sobre bens e serviços da reforma tributária), em reais. Informe só se constar no documento")
        @PositiveOrZero
        BigDecimal cbs,

        @JsonPropertyDescription("Imposto Seletivo (reforma tributária), em reais. Informe só se constar no documento")
        @PositiveOrZero
        BigDecimal selectiveTax
    ){}

    @Tool(description = "Lança uma nota fiscal no sistema do usuário depois de conferir os dados extraídos do documento")
    public String releasesStatement(@ToolParam(description = "Número da nota fiscal, como aparece no documento. Ex.: 1003") String numberNf,
                                    @ToolParam(description = "Série da nota. Se o documento não trouxer série, não informe") String serie,
                                    @ToolParam(description = "Tipo da nota: SERVICO para prestação de serviço ou PRODUTO para venda de mercadoria") InvoiceTypes type,
                                    @ToolParam(description = "Situação da nota: EMITIDA, PENDENTE ou CANCELADA")InvoiceStatus status,
                                    @ToolParam(description = "Nome ou razão social do cliente (tomador) como aparece no documento") String clientName,
                                    @ToolParam(description = "CNPJ do cliente, com ou sem pontuação. Ex.: 12.345.678/0001-90") String clientCNPJ,
                                    @ToolParam(description = "Descrição do serviço prestado ou do produto vendido, em até 255 caracteres") String description,
                                    @ToolParam(description = "Valor total da nota em reais, como número com ponto decimal. Ex.: 8500.00") BigDecimal totalValue,
                                    @ToolParam(description = "Valor total dos impostos da nota em reais, como número com ponto decimal. Ex.: 765.00") BigDecimal fullTax,
                                    @ToolParam(description = "Data de emissão no formato yyyy-MM-dd. Ex.: 2026-10-02") String emission,
                                    @ToolParam(description = "Data de vencimento no formato yyyy-MM-dd. Informe só se constar no documento") String dueDate,
                                    @ToolParam(description = "E-mail do destinatário da nota. Informe só se constar no documento") String recipientMail,
                                    @ToolParam(description = "Observações da nota, em até 500 caracteres. Informe só se constar no documento") String obs,
                                    @ToolParam(description = "Impostos da nota, cada um em reais, como número com ponto decimal. Informe apenas os que constam no documento; os demais deixe vazios", required = false) FiscalTax fiscalTax,
                                    ToolContext toolContext){

        if (!(totalValue.compareTo(BigDecimal.ZERO) > 0)){
            throw new ValueZeradoOrLesserThanZeroException("The value came back as zero or less than 0.");
        }

        String cnpj = normalizeCNPJ(clientCNPJ);
        if (!isValidCNPJ(cnpj)) {
            throw new InvalidCNPJException("Invalid CNPJ: " + clientCNPJ);
        }

        LocalDate emissionFormat = LocalDate.parse(emission);
        LocalDate dueDateFormat = null;
        if (dueDate != null && !dueDate.isBlank()) {
            dueDateFormat = LocalDate.parse(dueDate);
            if (dueDateFormat.isBefore(emissionFormat)) {
                throw new DueDateBeforeEmissionException("The due date is before the issuance date");
            }
        }

        if (status == CANCELADA) {
            throw new InvoiceStatusException("It was not possible to post the invoice because its status is \"cancelled.\"");
        }

        if (fullTax.compareTo(totalValue) > 0) {
            throw new TaxAmountHigherThanTotalAmount("The tax amount is greater than the total value of the invoice.");
        }

        calculateTotalTax(fiscalTax, fullTax);

        checkForDuplicates(numberNf);

        long userId = (long) toolContext.getContext().get("userId");

        FiscalTax tax = fiscalTax != null ? fiscalTax
                : new FiscalTax(null, null, null, null, null, null, null, null, null, null, null);

        Invoice invoice = new Invoice(userId, numberNf, serie, type, status,
                clientName, cnpj, recipientMail, description,
                obs, totalValue, fullTax,
                emissionFormat, dueDateFormat,
                tax.icms(), tax.ipi(), tax.pis(), tax.cofins(),
                tax.iss(), tax.irrf(), tax.csll(), tax.inss(),
                tax.ibs(), tax.cbs(), tax.selectiveTax());

        fiscalAgentRepository.save(invoice);

        return "Nota fiscal salva!";
    }

    private void checkForDuplicates(String numberNf) {
        Optional<Invoice> duplicated = fiscalAgentRepository.searchInvoiceNumber(numberNf);
        if (duplicated.isPresent()) {
            throw new DuplicateInvoiceException("Invoice " + numberNf + " has already been registered.");
        }
    }

    private static void calculateTotalTax(FiscalTax fiscalTax, BigDecimal fullTax){
        if (fiscalTax == null) {
            return;
        }

        BigDecimal sumTax = BigDecimal.ZERO;

        sumTax = sumTax.add(valueOrZero(fiscalTax.icms()));
        sumTax = sumTax.add(valueOrZero(fiscalTax.ipi()));
        sumTax = sumTax.add(valueOrZero(fiscalTax.pis()));
        sumTax = sumTax.add(valueOrZero(fiscalTax.cofins()));
        sumTax = sumTax.add(valueOrZero(fiscalTax.iss()));
        sumTax = sumTax.add(valueOrZero(fiscalTax.irrf()));
        sumTax = sumTax.add(valueOrZero(fiscalTax.csll()));
        sumTax = sumTax.add(valueOrZero(fiscalTax.inss()));
        sumTax = sumTax.add(valueOrZero(fiscalTax.ibs()));
        sumTax = sumTax.add(valueOrZero(fiscalTax.cbs()));
        sumTax = sumTax.add(valueOrZero(fiscalTax.selectiveTax()));

        if (sumTax.compareTo(fullTax) != 0){
            throw new FullTaxException("The value of the unit taxes differs from the total tax.");
        }
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static final int[] CNPJ_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    /** Valida CNPJ numérico e alfanumérico (14 caracteres, já normalizado). */
    static boolean isValidCNPJ(String cnpj) {
        if (cnpj == null || !cnpj.matches("[0-9A-Z]{12}[0-9]{2}")) {
            return false;
        }
        // rejeita sequências repetidas (ex.: 00000000000000)
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }

        int first = checkDigit(cnpj, 12);
        int second = checkDigit(cnpj, 13);
        return first == cnpj.charAt(12) - '0' && second == cnpj.charAt(13) - '0';
    }

    private static int checkDigit(String cnpj, int length) {
        int sum = 0;
        int offset = CNPJ_WEIGHTS.length - length;
        for (int i = 0; i < length; i++) {
            sum += (cnpj.charAt(i) - '0') * CNPJ_WEIGHTS[i + offset];
        }
        int rest = sum % 11;
        return rest < 2 ? 0 : 11 - rest;
    }

    private static String normalizeCNPJ(String cnpj){
        return cnpj.trim().replaceAll("[.\\-/]", "").toUpperCase();
    }

    @Tool(description = "Busca uma nota fiscal já lançada no sistema pelo número e retorna seus dados (cliente, CNPJ, tipo, situação, valores, impostos, datas e observações). Use quando o usuário pedir para consultar, conferir ou mostrar uma nota, ou antes de alterá-la. Se não existir nota com esse número, informa que não foi encontrada")
    public Invoice searchInvoice(@ToolParam(description = "Número da nota fiscal a consultar, somente dígitos, como aparece no documento. Ex.: 1003") String numberNf){
        return fiscalAgentRepository.findByNumber(numberNf)
                .orElseThrow(() -> new NoteNotFoundException("No invoice with this number was found."));
    }

    @Tool(description = "Lista todas as notas fiscais já lançadas no sistema que estão em uma determinada situação (EMITIDA, PENDENTE ou CANCELADA). Use quando o usuário pedir para ver, listar ou contar notas por situação, por exemplo \"quais notas estão pendentes?\". Para consultar uma nota específica pelo número, use a busca por número")
    public List<Invoice> searchPerStatus(@ToolParam(description = "Situação das notas a listar: EMITIDA, PENDENTE ou CANCELADA") InvoiceStatus invoiceStatus){
        return fiscalAgentRepository.findByStatus(invoiceStatus);
    }
}

