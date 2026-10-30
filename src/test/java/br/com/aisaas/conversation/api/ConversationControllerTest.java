package br.com.aisaas.conversation.api;

import br.com.aisaas.conversation.api.dto.AlterTitleRequest;
import br.com.aisaas.conversation.api.dto.CreateConversationRequest;
import br.com.aisaas.conversation.application.ChatService;
import br.com.aisaas.conversation.application.ConversationCommandService;
import br.com.aisaas.conversation.application.ConversationQueryService;
import br.com.aisaas.conversation.domain.Conversation;
import br.com.aisaas.conversation.domain.ConversationStatus;
import br.com.aisaas.conversation.domain.Message;
import br.com.aisaas.conversation.domain.MessageRole;
import br.com.aisaas.conversation.domain.exceptions.ConversationNotFoundException;
import br.com.aisaas.conversation.domain.exceptions.GeminiAPINotRespondException;
import br.com.aisaas.conversation.domain.exceptions.InvalidRouterResponseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConversationControllerTest {

    private static final long USER_ID = 7L;

    private ConversationQueryService queryService;
    private ConversationCommandService commandService;
    private ChatService chatService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        queryService = mock(ConversationQueryService.class);
        commandService = mock(ConversationCommandService.class);
        chatService = mock(ChatService.class);
        mvc = MockMvcBuilders.standaloneSetup(new ConversationController(queryService, commandService, chatService))
                .setControllerAdvice(new ConversationControllerAdvice())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(String.valueOf(USER_ID)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Conversation conversation(long id, String title, ConversationStatus status) {
        Conversation conversation = new Conversation();
        ReflectionTestUtils.setField(conversation, "id", id);
        conversation.setUserId(USER_ID);
        conversation.setTitle(title);
        conversation.setStatus(status);
        conversation.setCreatedAt(LocalDateTime.of(2026, 10, 20, 12, 0));
        return conversation;
    }

    private Message message(long id, MessageRole role, Long agentId, String content) {
        Message message = new Message();
        ReflectionTestUtils.setField(message, "id", id);
        message.setConversationId(1L);
        message.setRole(role);
        message.setAgentId(agentId);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.of(2026, 10, 20, 12, 0));
        return message;
    }

    // --- GET /api/conversations ---

    @Test
    void listsTheConversationsOfTheLoggedUser() throws Exception {
        when(queryService.listAllChats(USER_ID)).thenReturn(List.of(
                conversation(1L, "first", ConversationStatus.ACTIVE),
                conversation(2L, "second", ConversationStatus.ARCHIVED)));

        mvc.perform(get("/api/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("first"))
                .andExpect(jsonPath("$[1].status").value("ARCHIVED"));
    }

    // --- GET /api/conversations/{id} ---

    @Test
    void returnsTheConversationWithItsLastMessages() throws Exception {
        when(queryService.findById(1L, USER_ID)).thenReturn(conversation(1L, "first", ConversationStatus.ACTIVE));
        when(chatService.lastMessages(1L, 30)).thenReturn(List.of(
                message(10L, MessageRole.USER, null, "hi"),
                message(11L, MessageRole.ASSISTANT, 2L, "hello")));

        mvc.perform(get("/api/conversations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("first"))
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[0].role").value("USER"))
                .andExpect(jsonPath("$.messages[1].agentId").value(2))
                .andExpect(jsonPath("$.messages[1].content").value("hello"));
    }

    @Test
    void returns404WhenTheConversationDoesNotExist() throws Exception {
        when(queryService.findById(99L, USER_ID)).thenThrow(new ConversationNotFoundException("not found"));

        mvc.perform(get("/api/conversations/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("not found"));
    }

    // --- GET /api/conversations/{id}/messages?before= ---

    @Test
    void returnsTheMessagesBeforeTheGivenOne() throws Exception {
        when(queryService.findById(1L, USER_ID)).thenReturn(conversation(1L, "first", ConversationStatus.ACTIVE));
        when(chatService.lastMessagesBefore(1L, 10L)).thenReturn(List.of(message(5L, MessageRole.USER, null, "older")));

        mvc.perform(get("/api/conversations/1/messages").param("before", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages.length()").value(1))
                .andExpect(jsonPath("$.messages[0].content").value("older"));
    }

    @Test
    void returns400WhenTheBeforeParamIsMissing() throws Exception {
        mvc.perform(get("/api/conversations/1/messages"))
                .andExpect(status().isBadRequest());
    }

    // --- POST /api/conversations ---

    @Test
    void createsAConversationForTheLoggedUser() throws Exception {
        when(commandService.createConversation(any(CreateConversationRequest.class), eq(USER_ID)))
                .thenReturn(conversation(3L, "Nova conversa", ConversationStatus.ACTIVE));

        mvc.perform(post("/api/conversations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Nova conversa\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.title").value("Nova conversa"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void returns400WhenCreatingWithoutATitle() throws Exception {
        mvc.perform(post("/api/conversations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commandService);
    }

    @Test
    void returns400WhenTheTitleIsLongerThanTheLimit() throws Exception {
        String longTitle = "a".repeat(101);

        mvc.perform(post("/api/conversations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + longTitle + "\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isBadRequest());
    }

    // --- PATCH /api/conversations/{id} ---

    @Test
    void renamesTheConversation() throws Exception {
        when(commandService.renameTitle(eq(1L), any(AlterTitleRequest.class), eq(USER_ID)))
                .thenReturn(conversation(1L, "Renamed", ConversationStatus.ACTIVE));

        mvc.perform(patch("/api/conversations/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newTitle\":\"Renamed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Renamed"));
    }

    @Test
    void returns400WhenRenamingToABlankTitle() throws Exception {
        mvc.perform(patch("/api/conversations/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newTitle\":\"  \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commandService);
    }

    @Test
    void returns404WhenRenamingAnUnknownConversation() throws Exception {
        when(commandService.renameTitle(eq(99L), any(AlterTitleRequest.class), eq(USER_ID)))
                .thenThrow(new ConversationNotFoundException("not found"));

        mvc.perform(patch("/api/conversations/99").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newTitle\":\"x\"}"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/conversations/{id}/archive | unarchive ---

    @Test
    void archivesTheConversation() throws Exception {
        mvc.perform(post("/api/conversations/1/archive"))
                .andExpect(status().isNoContent());

        verify(commandService).alterStatus(1L, ConversationStatus.ARCHIVED, USER_ID);
    }

    @Test
    void unarchivesTheConversation() throws Exception {
        mvc.perform(post("/api/conversations/1/unarchive"))
                .andExpect(status().isNoContent());

        verify(commandService).alterStatus(1L, ConversationStatus.ACTIVE, USER_ID);
    }

    @Test
    void returns404WhenArchivingAnUnknownConversation() throws Exception {
        when(commandService.alterStatus(99L, ConversationStatus.ARCHIVED, USER_ID))
                .thenThrow(new ConversationNotFoundException("not found"));

        mvc.perform(post("/api/conversations/99/archive"))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /api/conversations/{id} ---

    @Test
    void deletesTheConversation() throws Exception {
        mvc.perform(delete("/api/conversations/1"))
                .andExpect(status().isNoContent());

        verify(commandService).deleteConversation(1L, USER_ID);
    }

    @Test
    void returns404WhenDeletingAnUnknownConversation() throws Exception {
        doThrow(new ConversationNotFoundException("not found")).when(commandService).deleteConversation(99L, USER_ID);

        mvc.perform(delete("/api/conversations/99"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/conversations/{id}/messages (validation and errors before the stream starts) ---

    @Test
    void returns400WhenSendingAMessageWithoutContent() throws Exception {
        mvc.perform(post("/api/conversations/1/messages").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(chatService);
    }

    @Test
    void returns404WhenSendingAMessageToAnUnknownConversation() throws Exception {
        when(chatService.orquestratorMessage(eq(99L), any(), eq(USER_ID), isNull()))
                .thenThrow(new ConversationNotFoundException("not found"));

        mvc.perform(post("/api/conversations/99/messages").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hi\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns409WhenTheRouterDoesNotReturnAValidAgent() throws Exception {
        when(commandService.findByIdAndUserId(1L, USER_ID)).thenReturn(conversation(1L, "first", ConversationStatus.ACTIVE));
        when(chatService.orquestratorMessage(eq(1L), any(), eq(USER_ID), isNull()))
                .thenThrow(new InvalidRouterResponseException("bad router answer"));

        mvc.perform(post("/api/conversations/1/messages").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hi\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NO_AGENT"));
    }

    @Test
    void returns500WhenTheGeminiApiDoesNotRespond() throws Exception {
        when(commandService.findByIdAndUserId(1L, USER_ID)).thenReturn(conversation(1L, "first", ConversationStatus.ACTIVE));
        when(chatService.orquestratorMessage(eq(1L), any(), eq(USER_ID), isNull()))
                .thenThrow(new GeminiAPINotRespondException("The Gemini API not respond"));

        mvc.perform(post("/api/conversations/1/messages").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hi\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("TECHNICAL_ERROR"));
    }
}
