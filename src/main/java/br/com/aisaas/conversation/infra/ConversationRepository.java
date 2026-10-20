package br.com.aisaas.conversation.infra;

import br.com.aisaas.conversation.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByIdAndUserId(long id, long userId);
    List<Conversation> findByUserId(long userId);
}
