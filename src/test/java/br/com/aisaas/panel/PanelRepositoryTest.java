package br.com.aisaas.panel;

import br.com.aisaas.conversation.domain.Conversation;
import br.com.aisaas.conversation.domain.Message;
import br.com.aisaas.conversation.domain.MessageRole;
import br.com.aisaas.conversation.infra.ConversationRepository;
import br.com.aisaas.conversation.infra.MessageRepository;
import br.com.aisaas.panel.api.dto.AgentUsageResponse;
import br.com.aisaas.panel.api.dto.CardsResponse;
import br.com.aisaas.panel.api.dto.ConversationsCreated;
import br.com.aisaas.panel.api.dto.DailyTokens;
import br.com.aisaas.panel.api.dto.ResponseTimeResponse;
import br.com.aisaas.panel.infra.PanelRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PanelRepositoryTest {

    @Autowired
    private PanelRepository panelRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private EntityManager entityManager;

    private Conversation saveConversation() {
        Conversation conversation = new Conversation();
        conversation.setUserId(1L);
        conversation.setTitle("Nova conversa");
        return conversationRepository.save(conversation);
    }

    private Message saveMessage(long conversationId, MessageRole role, Long agentId, int totalToken, int latencyMs) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setAgentId(agentId);
        message.setContent("texto");
        message.setTotalToken(totalToken);
        message.setLatencyMs(latencyMs);
        return messageRepository.save(message);
    }

    private void backdateConversation(long conversationId, int daysAgo) {
        entityManager.flush();
        entityManager.createNativeQuery("update conversations set created_at = ?1 where id = ?2")
                .setParameter(1, LocalDateTime.now().minusDays(daysAgo))
                .setParameter(2, conversationId)
                .executeUpdate();
        entityManager.clear();
    }

    @Test
    void cardsWithoutDataReturnsZeros() {
        CardsResponse cards = panelRepository.cards();

        assertThat(cards.totalConversations()).isZero();
        assertThat(cards.totalMessages()).isZero();
        assertThat(cards.totalTokens()).isZero();
    }

    @Test
    void cardsCountsConversationsMessagesAndTokens() {
        Conversation first = saveConversation();
        saveConversation();
        saveMessage(first.getId(), MessageRole.USER, null, 0, 0);
        saveMessage(first.getId(), MessageRole.ASSISTANT, 1L, 10, 100);
        saveMessage(first.getId(), MessageRole.ASSISTANT, 1L, 20, 300);

        CardsResponse cards = panelRepository.cards();

        assertThat(cards.totalConversations()).isEqualTo(2);
        assertThat(cards.totalMessages()).isEqualTo(3);
        assertThat(cards.totalTokens()).isEqualTo(30);
    }

    @Test
    void dailyTokenGroupsByDay() {
        Conversation conversation = saveConversation();
        saveMessage(conversation.getId(), MessageRole.ASSISTANT, 1L, 10, 100);
        saveMessage(conversation.getId(), MessageRole.ASSISTANT, 1L, 20, 300);

        List<DailyTokens> result = panelRepository.dailyToken();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.now());
        assertThat(result.get(0).totalTokens()).isEqualTo(30);
    }

    @Test
    void conversationsCreatedOnlyIncludesLastFourteenDays() {
        saveConversation();
        saveConversation();
        Conversation old = saveConversation();
        backdateConversation(old.getId(), 20);

        List<ConversationsCreated> result = panelRepository.convesationsCreated();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.now());
        assertThat(result.get(0).quantityConversations()).isEqualTo(2);
    }

    @Test
    void agentUsageIgnoresMessagesWithoutAgent() {
        Conversation conversation = saveConversation();
        saveMessage(conversation.getId(), MessageRole.USER, null, 0, 0);
        saveMessage(conversation.getId(), MessageRole.ASSISTANT, 1L, 10, 100);
        saveMessage(conversation.getId(), MessageRole.ASSISTANT, 1L, 10, 100);
        saveMessage(conversation.getId(), MessageRole.ASSISTANT, 2L, 10, 100);

        List<AgentUsageResponse> result = panelRepository.agentUsage();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AgentUsageResponse::agentId).containsExactly(1L, 2L);
        assertThat(result).extracting(AgentUsageResponse::totalUsage).containsExactly(2L, 1L);
    }

    @Test
    void responseTimeAveragesOnlyAssistantMessages() {
        Conversation conversation = saveConversation();
        saveMessage(conversation.getId(), MessageRole.USER, null, 0, 5000);
        saveMessage(conversation.getId(), MessageRole.ASSISTANT, 1L, 10, 100);
        saveMessage(conversation.getId(), MessageRole.ASSISTANT, 1L, 10, 300);

        List<ResponseTimeResponse> result = panelRepository.responseTime();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.now());
        assertThat(result.get(0).latencyMs()).isEqualTo(200.0);
    }
}
