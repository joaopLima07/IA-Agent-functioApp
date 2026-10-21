package br.com.aisaas.conversation.application;

import br.com.aisaas.agent.application.AgentQueryService;
import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.conversation.api.dto.SendMessageRequest;
import br.com.aisaas.conversation.domain.Conversation;
import br.com.aisaas.conversation.domain.Message;
import br.com.aisaas.conversation.domain.MessageRole;
import br.com.aisaas.conversation.domain.exceptions.ConversationNotFoundException;
import br.com.aisaas.conversation.domain.exceptions.InvalidRouterResponseException;
import br.com.aisaas.conversation.infra.ConversationRepository;
import br.com.aisaas.conversation.infra.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private MessageRepository messageRepository;
    private ConversationRepository conversationRepository;
    private AgentQueryService agentQueryService;
    private AgentRouter agentRouter;
    private ReplyGeneretor replyGeneretor;
    private ChatService service;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        conversationRepository = mock(ConversationRepository.class);
        agentQueryService = mock(AgentQueryService.class);
        agentRouter = mock(AgentRouter.class);
        replyGeneretor = mock(ReplyGeneretor.class);
        service = new ChatService(messageRepository, conversationRepository, agentQueryService, replyGeneretor, agentRouter);
    }

    private Message message(MessageRole role, String content, LocalDateTime createdAt) {
        Message message = new Message();
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(createdAt);
        return message;
    }

    private ChatResponse chunk(String text, DefaultUsage usage) {
        ChatResponse.Builder builder = ChatResponse.builder()
                .generations(List.of(new Generation(new AssistantMessage(text))));
        if (usage != null) {
            builder.metadata(ChatResponseMetadata.builder().usage(usage).build());
        }
        return builder.build();
    }

    @Test
    void lastMessagesReturnsTheMessagesOldestFirst() {
        LocalDateTime now = LocalDateTime.now();
        Message newest = message(MessageRole.USER, "newest", now);
        Message oldest = message(MessageRole.USER, "oldest", now.minusMinutes(5));
        when(messageRepository.searchLastMessages(eq(1L), any(Pageable.class))).thenReturn(List.of(newest, oldest));

        List<Message> result = service.lastMessages(1L, 5);

        assertEquals(List.of(oldest, newest), result);
    }

    @Test
    void lastMessagesAsksTheRepositoryForThePageSize() {
        when(messageRepository.searchLastMessages(eq(1L), any(Pageable.class))).thenReturn(List.of());

        service.lastMessages(1L, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).searchLastMessages(eq(1L), captor.capture());
        assertEquals(5, captor.getValue().getPageSize());
        assertEquals(0, captor.getValue().getPageNumber());
    }

    @Test
    void lastMessagesBeforeReturnsTheMessagesOldestFirstWithAPageOfThirty() {
        LocalDateTime now = LocalDateTime.now();
        Message newer = message(MessageRole.USER, "newer", now);
        Message older = message(MessageRole.USER, "older", now.minusMinutes(5));
        when(messageRepository.searchBeforeMessages(eq(1L), eq(10L), any(Pageable.class))).thenReturn(List.of(newer, older));

        List<Message> result = service.lastMessagesBefore(1L, 10L);

        assertEquals(List.of(older, newer), result);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).searchBeforeMessages(eq(1L), eq(10L), captor.capture());
        assertEquals(30, captor.getValue().getPageSize());
    }

    @Test
    void checkMessageReturnsTheConversationOfTheUser() {
        Conversation conversation = new Conversation();
        when(conversationRepository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(conversation));

        assertSame(conversation, service.checkMessage(1L, 7L));
    }

    @Test
    void checkMessageThrowsWhenTheConversationIsNotTheUsers() {
        when(conversationRepository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class, () -> service.checkMessage(1L, 7L));
    }

    @Test
    void toAiMessagesMapsRolesAndKeepsTheOrder() {
        List<Message> history = List.of(
                message(MessageRole.USER, "hello", LocalDateTime.now()),
                message(MessageRole.ASSISTANT, "hi there", LocalDateTime.now()));

        List<org.springframework.ai.chat.messages.Message> result = service.toAiMessages(history);

        assertEquals(2, result.size());
        assertInstanceOf(UserMessage.class, result.get(0));
        assertEquals("hello", result.get(0).getText());
        assertInstanceOf(AssistantMessage.class, result.get(1));
        assertEquals("hi there", result.get(1).getText());
    }

    @Test
    void parseAgentIdAcceptsAnIdWithSurroundingWhitespace() {
        assertEquals(12L, service.parseAgentId("  12\n"));
    }

    @Test
    void parseAgentIdRejectsNullAndBlank() {
        assertThrows(InvalidRouterResponseException.class, () -> service.parseAgentId(null));
        assertThrows(InvalidRouterResponseException.class, () -> service.parseAgentId("   "));
    }

    @Test
    void parseAgentIdRejectsANonNumericResponse() {
        assertThrows(InvalidRouterResponseException.class, () -> service.parseAgentId("agent two"));
    }

    @Test
    void orquestratorMessageThrowsWhenTheConversationIsNotTheUsers() {
        when(conversationRepository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class,
                () -> service.orquestratorMessage(1L, new SendMessageRequest("hi"), 7L));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void orquestratorMessageRejectsARouterResponseThatIsNotAnAgentId() {
        Conversation conversation = new Conversation();
        ReflectionTestUtils.setField(conversation, "id", 1L);
        when(conversationRepository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.searchLastMessages(anyLong(), any(Pageable.class))).thenReturn(List.of());
        when(agentQueryService.searchAll()).thenReturn(List.of());
        when(agentQueryService.findRouterAgent()).thenReturn(new Agent());
        when(agentRouter.choseAgent(any(), anyString(), anyString()))
                .thenReturn(chunk("no agent fits", new DefaultUsage(1, 1, 2)));

        assertThrows(InvalidRouterResponseException.class,
                () -> service.orquestratorMessage(1L, new SendMessageRequest("hi"), 7L));
    }

    @Test
    void orquestratorMessageStreamsTheReplyAndSavesBothMessages() {
        Conversation conversation = new Conversation();
        ReflectionTestUtils.setField(conversation, "id", 1L);
        Agent routerAgent = new Agent();
        Agent chosenAgent = new Agent();
        chosenAgent.setId(2L);

        when(conversationRepository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.searchLastMessages(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(message(MessageRole.USER, "hi", LocalDateTime.now())));
        when(agentQueryService.searchAll()).thenReturn(List.of(chosenAgent));
        when(agentQueryService.findRouterAgent()).thenReturn(routerAgent);
        when(agentQueryService.searchUnique(2L)).thenReturn(chosenAgent);
        when(agentRouter.choseAgent(any(), anyString(), anyString()))
                .thenReturn(chunk("2", new DefaultUsage(10, 5, 15)));
        when(replyGeneretor.finalMessage(any(), any())).thenReturn(Flux.just(
                chunk("Hello, ", null),
                chunk("world", new DefaultUsage(20, 10, 30))));

        SseEmitter emitter = service.orquestratorMessage(1L, new SendMessageRequest("hi"), 7L);

        assertNotNull(emitter);
        // the reply is consumed on another thread, so wait for the assistant message to be saved
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, timeout(3000).times(2)).save(captor.capture());

        Message user = captor.getAllValues().get(0);
        assertEquals(MessageRole.USER, user.getRole());
        assertEquals("hi", user.getContent());

        Message assistant = captor.getAllValues().get(1);
        assertEquals(MessageRole.ASSISTANT, assistant.getRole());
        assertEquals("Hello, world", assistant.getContent());
        assertEquals(2L, assistant.getAgentId());
        assertEquals(1L, assistant.getConversationId());
        // tokens of the router call are added to the ones of the reply
        assertEquals(30, assistant.getInputToken());
        assertEquals(15, assistant.getOutputToken());
        assertEquals(45, assistant.getTotalToken());
    }

    @Test
    void orquestratorMessageDoesNotSaveAnAssistantMessageWhenTheStreamFailsBeforeAnyText() {
        Conversation conversation = new Conversation();
        ReflectionTestUtils.setField(conversation, "id", 1L);
        Agent chosenAgent = new Agent();
        chosenAgent.setId(2L);

        when(conversationRepository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.searchLastMessages(anyLong(), any(Pageable.class))).thenReturn(List.of());
        when(agentQueryService.searchAll()).thenReturn(List.of());
        when(agentQueryService.findRouterAgent()).thenReturn(new Agent());
        when(agentQueryService.searchUnique(2L)).thenReturn(chosenAgent);
        when(agentRouter.choseAgent(any(), anyString(), anyString()))
                .thenReturn(chunk("2", new DefaultUsage(10, 5, 15)));
        when(replyGeneretor.finalMessage(any(), any())).thenReturn(Flux.error(new RuntimeException("gemini down")));

        service.orquestratorMessage(1L, new SendMessageRequest("hi"), 7L);

        // only the user message is saved; give the async stream time to fail before checking
        verify(messageRepository, timeout(1000).times(1)).save(any(Message.class));
        verify(messageRepository, after(500).times(1)).save(any(Message.class));
    }
}
