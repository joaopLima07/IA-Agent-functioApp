package br.com.aisaas.conversation.infra;

import br.com.aisaas.conversation.domain.Conversation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    private Conversation save(long userId, String title) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        return conversationRepository.save(conversation);
    }

    @Test
    void findByIdAndUserIdReturnsConversationOfTheOwner() {
        Conversation saved = save(1L, "my conversation");

        assertTrue(conversationRepository.findByIdAndUserId(saved.getId(), 1L).isPresent());
    }

    @Test
    void findByIdAndUserIdDoesNotReturnConversationOfAnotherUser() {
        Conversation saved = save(1L, "my conversation");

        assertTrue(conversationRepository.findByIdAndUserId(saved.getId(), 2L).isEmpty());
    }

    @Test
    void findByUserIdReturnsOnlyConversationsOfThatUser() {
        save(1L, "a");
        save(1L, "b");
        save(2L, "c");

        List<Conversation> result = conversationRepository.findByUserId(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.getUserId() == 1L));
    }
}
