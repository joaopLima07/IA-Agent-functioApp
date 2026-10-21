package br.com.aisaas.conversation.infra;

import br.com.aisaas.conversation.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("select m from Message m where m.conversationId = :conversationId order by m.createdAt desc")
    List<Message> searchLastMessages(
            @Param("conversationId") long conversationId,
            Pageable pageable);


    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId AND m.id < :id ORDER BY m.id DESC")
    List<Message> searchBeforeMessages(
            @Param("conversationId") long conversationId,
            @Param("id") long id,
            Pageable pageable
    );

    @Query("""
            select m from Message m
            where m.role = br.com.aisaas.conversation.domain.MessageRole.USER
              and m.id in (
                select max(m2.id) from Message m2
                where m2.role = br.com.aisaas.conversation.domain.MessageRole.USER
                  and m2.conversationId in :conversationIds
                group by m2.conversationId)
            """)
    List<Message> lastUserMessages(@Param("conversationIds") List<Long> conversationIds);
}
