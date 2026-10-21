package br.com.aisaas.conversation.application;

import br.com.aisaas.conversation.domain.Conversation;
import br.com.aisaas.conversation.domain.exceptions.ConversationNotFoundException;
import br.com.aisaas.conversation.infra.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConversationQueryServiceTest {

    private ConversationRepository repository;
    private ConversationQueryService service;

    @BeforeEach
    void setUp() {
        repository = mock(ConversationRepository.class);
        service = new ConversationQueryService(repository);
    }

    @Test
    void listAllChatsReturnsTheConversationsOfTheUser() {
        List<Conversation> conversations = List.of(new Conversation(), new Conversation());
        when(repository.findByUserId(7L)).thenReturn(conversations);

        assertSame(conversations, service.listAllChats(7L));
    }

    @Test
    void findByIdReturnsTheConversation() {
        Conversation conversation = new Conversation();
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(conversation));

        assertSame(conversation, service.findById(1L, 7L));
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class, () -> service.findById(1L, 7L));
    }
}
