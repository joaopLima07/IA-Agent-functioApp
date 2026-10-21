package br.com.aisaas.conversation.api;

import br.com.aisaas.conversation.api.dto.*;
import br.com.aisaas.conversation.application.ChatService;
import br.com.aisaas.conversation.application.ConversationCommandService;
import br.com.aisaas.conversation.application.ConversationQueryService;
import br.com.aisaas.conversation.domain.Conversation;
import br.com.aisaas.conversation.domain.ConversationStatus;
import br.com.aisaas.conversation.domain.Message;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;


@RestController
public class ConversationController {
    private final ConversationQueryService conversationQueryService;
    private final ConversationCommandService conversationCommandService;
    private final ChatService chatService;

    public ConversationController(ConversationQueryService conversationQueryService, ConversationCommandService conversationCommandService, ChatService chatService) {
        this.conversationQueryService = conversationQueryService;
        this.conversationCommandService = conversationCommandService;
        this.chatService = chatService;
    }

    @GetMapping("/api/conversations")
    public ResponseEntity<List<ConversationSummaryResponse>> listAllConversations(@AuthenticationPrincipal Jwt jwt){

        long userId = Long.parseLong(jwt.getSubject());

        List<Conversation> conversations = conversationQueryService.listAllChats(userId);

        java.util.Map<Long, String> lastMessages = chatService.lastUserMessages(conversations.stream().map(Conversation::getId).toList());

        List<ConversationSummaryResponse> conversationSummaryResponse = conversations.stream()
                .map(n -> new ConversationSummaryResponse(
                        n.getId(),
                        n.getUserId(),
                        n.getTitle(),
                        n.getStatus(),
                        lastMessages.get(n.getId())
                ))
                .toList();

        return ResponseEntity.ok().body(conversationSummaryResponse);
    }

    @GetMapping("/api/conversations/{id}")
    public ResponseEntity<ConversationDetailResponse> listUniqueConversation(@PathVariable long id, @AuthenticationPrincipal Jwt jwt){

        long userId = Long.parseLong(jwt.getSubject());

        Conversation uniqueConversation = conversationQueryService.findById(id, userId);

        List<Message> last30Messages = chatService.lastMessages(id, 30);

        ConversationDetailResponse conversationDetail = new ConversationDetailResponse(uniqueConversation.getId(),
                uniqueConversation.getUserId(),
                uniqueConversation.getTitle(),
                uniqueConversation.getStatus(),
                last30Messages.stream()
                        .map(n -> new ConversationDetailResponse.MessagesResponse(
                                n.getId(),
                                n.getConversationId(),
                                n.getRole(),
                                n.getAgentId(),
                                n.getContent(),
                                n.getCreatedAt()
                        ))
                        .toList()
        );

        return ResponseEntity.ok().body(conversationDetail);
    }

    @GetMapping("/api/conversations/{id}/messages")
    public ResponseEntity<ConversationDetailResponse> listLastThirty(@PathVariable long id, @RequestParam("before") long messageId, @AuthenticationPrincipal Jwt jwt){
        long userId = Long.parseLong(jwt.getSubject());

        Conversation conversation = conversationQueryService.findById(id, userId);

        List<Message> lastBefore = chatService.lastMessagesBefore(id, messageId);


        return ResponseEntity.ok().body(new ConversationDetailResponse(
                conversation.getId(),
                conversation.getUserId(),
                conversation.getTitle(),
                conversation.getStatus(),
                lastBefore.stream()
                        .map(n -> new ConversationDetailResponse.MessagesResponse(
                                n.getId(),
                                n.getConversationId(),
                                n.getRole(),
                                n.getAgentId(),
                                n.getContent(),
                                n.getCreatedAt()
                        ))
                        .toList()
        ));
    }

    @PostMapping("/api/conversations")
    public ResponseEntity<CreateConversationResponse> createConversation(@Valid @RequestBody CreateConversationRequest createConversationRequest, @AuthenticationPrincipal Jwt jwt){
        long userId = Long.parseLong(jwt.getSubject());

        Conversation createResponse = conversationCommandService.createConversation(createConversationRequest, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateConversationResponse(createResponse.getId(),
                createResponse.getTitle(),
                createResponse.getStatus(),
                createResponse.getCreatedAt()));

    }

    @PatchMapping("/api/conversations/{id}")
    public ResponseEntity<ConversationSummaryResponse> renameConversation(@PathVariable long id, @Valid @RequestBody AlterTitleRequest alterTitleRequest, @AuthenticationPrincipal Jwt jwt){
        long userId = Long.parseLong(jwt.getSubject());

        Conversation renamed = conversationCommandService.renameTitle(id, alterTitleRequest, userId);

        return ResponseEntity.ok().body(new ConversationSummaryResponse(
                renamed.getId(),
                renamed.getUserId(),
                renamed.getTitle(),
                renamed.getStatus()
        ));
    }

    @PostMapping(value = "/api/conversations/{id}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessage(@PathVariable long id, @Valid @RequestBody SendMessageRequest sendMessageRequest, @AuthenticationPrincipal Jwt jwt){
        long userId = Long.parseLong(jwt.getSubject());

        Conversation conversation = conversationCommandService.findByIdAndUserId(id, userId);

        return chatService.orquestratorMessage(id, sendMessageRequest, userId);
    }


    @PostMapping("/api/conversations/{id}/archive")
    public ResponseEntity<Object> setArchiveConversation(@PathVariable long id, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.parseLong(jwt.getSubject());

        conversationCommandService.alterStatus(id, ConversationStatus.ARCHIVED, userId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/conversations/{id}/unarchive")
    public ResponseEntity<Object> setUnarchiveConversation(@PathVariable long id, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.parseLong(jwt.getSubject());

        conversationCommandService.alterStatus(id, ConversationStatus.ACTIVE, userId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/conversations/{id}")
    public ResponseEntity<Object> deleteConversation(@PathVariable long id, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.parseLong(jwt.getSubject());

        conversationCommandService.deleteConversation(id, userId);

        return ResponseEntity.noContent().build();
    }


}
