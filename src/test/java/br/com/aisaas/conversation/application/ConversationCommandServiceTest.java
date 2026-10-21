package br.com.aisaas.conversation.application;

import br.com.aisaas.conversation.api.dto.AlterTitleRequest;
import br.com.aisaas.conversation.api.dto.CreateConversationRequest;
import br.com.aisaas.conversation.domain.Conversation;
import br.com.aisaas.conversation.domain.ConversationStatus;
import br.com.aisaas.conversation.domain.exceptions.ConversationNotFoundException;
import br.com.aisaas.conversation.infra.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConversationCommandServiceTest {

    private ConversationRepository repository;
    private ConversationCommandService service;

    @BeforeEach
    void setUp() {
        repository = mock(ConversationRepository.class);
        service = new ConversationCommandService(repository);
        when(repository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Conversation existing(ConversationStatus status) {
        Conversation conversation = new Conversation();
        conversation.setUserId(7L);
        conversation.setTitle("title");
        conversation.setStatus(status);
        return conversation;
    }

    @Test
    void createConversationSavesWithTheUserTitleAndStatus() {
        service.createConversation(new CreateConversationRequest("Nova conversa", ConversationStatus.ACTIVE), 7L);

        ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(repository).save(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
        assertEquals("Nova conversa", captor.getValue().getTitle());
        assertEquals(ConversationStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    void findByIdAndUserIdThrowsWhenTheConversationDoesNotBelongToTheUser() {
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class, () -> service.findByIdAndUserId(1L, 7L));
    }

    @Test
    void deleteConversationDeletesTheFoundConversation() {
        Conversation found = existing(ConversationStatus.ACTIVE);
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(found));

        service.deleteConversation(1L, 7L);

        verify(repository).delete(found);
    }

    @Test
    void deleteConversationDoesNotDeleteWhenNotFound() {
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class, () -> service.deleteConversation(1L, 7L));

        verify(repository, never()).delete(any());
    }

    @Test
    void alterStatusArchivesAnActiveConversation() {
        Conversation found = existing(ConversationStatus.ACTIVE);
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(found));

        ConversationStatus result = service.alterStatus(1L, ConversationStatus.ARCHIVED, 7L);

        assertEquals(ConversationStatus.ARCHIVED, result);
        assertEquals(ConversationStatus.ARCHIVED, found.getStatus());
    }

    @Test
    void alterStatusUnarchivesAnArchivedConversation() {
        Conversation found = existing(ConversationStatus.ARCHIVED);
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(found));

        ConversationStatus result = service.alterStatus(1L, ConversationStatus.ACTIVE, 7L);

        assertEquals(ConversationStatus.ACTIVE, result);
        assertEquals(ConversationStatus.ACTIVE, found.getStatus());
    }

    @Test
    void alterStatusThrowsWhenNotFound() {
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class,
                () -> service.alterStatus(1L, ConversationStatus.ARCHIVED, 7L));

        verify(repository, never()).save(any());
    }

    @Test
    void renameTitleChangesTheTitleAndSaves() {
        Conversation found = existing(ConversationStatus.ACTIVE);
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(found));

        Conversation result = service.renameTitle(1L, new AlterTitleRequest("New title"), 7L);

        assertEquals("New title", result.getTitle());
        verify(repository).save(found);
    }

    @Test
    void renameTitleThrowsWhenNotFound() {
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class,
                () -> service.renameTitle(1L, new AlterTitleRequest("x"), 7L));
    }
}
