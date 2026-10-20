package br.com.aisaas.conversation.api;

import br.com.aisaas.conversation.api.dto.ConversationDetailResponse;
import br.com.aisaas.conversation.api.dto.ConversationSummaryResponse;
import br.com.aisaas.conversation.api.dto.CreateConversationRequest;
import br.com.aisaas.conversation.api.dto.CreateConversationResponse;
import br.com.aisaas.conversation.application.ChatService;
import br.com.aisaas.conversation.application.ConversationCommandService;
import br.com.aisaas.conversation.application.ConversationQueryService;
import br.com.aisaas.conversation.domain.Conversation;
import br.com.aisaas.conversation.domain.Message;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

        List<ConversationSummaryResponse> conversationSummaryResponse = conversationQueryService.listAllChats(userId).stream()
                .map(n -> new ConversationSummaryResponse(
                        n.getId(),
                        n.getUserId(),
                        n.getTitle(),
                        n.getStatus()
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

    //    Falta implementar o resto das rotas
    //    POST	/api/conversations/{id}/messages	{ content, agentId? }	200 (ou 201), a mensagem do assistente (MessageResponse)	401, 404, 409 (arquivada, agente inativo ou nenhum disponível), 422, erro de IA
    //    PATCH	/api/conversations/{id}	{ title }	200, ConversationSummaryResponse atualizado	401, 404, 422
    //    POST	/api/conversations/{id}/archive	Sem body	204	401, 404
    //    POST	/api/conversations/{id}/unarchive	Sem body	204	401, 404
    //    DELETE	/api/conversations/{id}	Sem body	204



}
