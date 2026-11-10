package br.com.aisaas.conversation.infra;

import br.com.aisaas.conversation.domain.Invoice;
import br.com.aisaas.conversation.domain.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FiscalAgentRepository extends JpaRepository<Invoice, Long> {

    @Query("select n from Invoice n where n.number = :numero")
    Optional<Invoice> searchInvoiceNumber(
            @Param("numero") String numberNf
    );

    Optional<Invoice> findByNumber(String numberNf);

    List<Invoice> findByStatus(InvoiceStatus status);
}
