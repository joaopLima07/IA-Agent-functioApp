package br.com.aisaas.conversation.infra;

import br.com.aisaas.conversation.domain.Message;
import br.com.aisaas.conversation.domain.MessageRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private EntityManager entityManager;

    private Message save(long conversationId, String content, int minutesAgo) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(MessageRole.USER);
        message.setContent(content);
        Message saved = messageRepository.saveAndFlush(message);

        // @CreationTimestamp always writes "now": force distinct dates so the ordering is deterministic
        entityManager.createNativeQuery("update message set created_at = ?1 where id = ?2")
                .setParameter(1, LocalDateTime.now().minusMinutes(minutesAgo))
                .setParameter(2, saved.getId())
                .executeUpdate();
        entityManager.clear();
        return saved;
    }

    private List<String> contents(List<Message> messages) {
        return messages.stream().map(Message::getContent).toList();
    }

    @Test
    void searchLastMessagesReturnsTheNewestFirstAndRespectsThePageSize() {
        save(1L, "oldest", 30);
        save(1L, "middle", 20);
        save(1L, "newest", 10);

        List<Message> result = messageRepository.searchLastMessages(1L, PageRequest.of(0, 2));

        assertEquals(List.of("newest", "middle"), contents(result));
    }

    @Test
    void searchLastMessagesIgnoresOtherConversations() {
        save(1L, "from conversation 1", 10);
        save(2L, "from conversation 2", 5);

        List<Message> result = messageRepository.searchLastMessages(1L, PageRequest.of(0, 10));

        assertEquals(List.of("from conversation 1"), contents(result));
    }

    @Test
    void searchBeforeMessagesReturnsOnlyOlderMessagesOfTheConversation() {
        save(1L, "first", 30);
        save(1L, "second", 20);
        Message third = save(1L, "third", 10);
        save(2L, "other conversation", 25);

        List<Message> result = messageRepository.searchBeforeMessages(1L, third.getId(), PageRequest.of(0, 30));

        assertEquals(List.of("second", "first"), contents(result));
    }

    @Test
    void searchBeforeMessagesRespectsThePageSize() {
        save(1L, "1", 40);
        save(1L, "2", 30);
        save(1L, "3", 20);
        Message last = save(1L, "4", 10);

        List<Message> result = messageRepository.searchBeforeMessages(1L, last.getId(), PageRequest.of(0, 1));

        assertEquals(List.of("3"), contents(result));
    }
}
