package br.com.aisaas.conversation.application;

import br.com.aisaas.conversation.api.dto.AlterTitleRequest;
import br.com.aisaas.conversation.api.dto.CreateConversationRequest;
import br.com.aisaas.conversation.domain.Conversation;
import br.com.aisaas.conversation.domain.ConversationStatus;
import br.com.aisaas.conversation.domain.exceptions.ConversationNotFoundException;
import br.com.aisaas.conversation.infra.ConversationRepository;
import org.springframework.stereotype.Service;

@Service
public class ConversationCommandService {
    private final ConversationRepository conversationRepository;

    public ConversationCommandService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public Conversation createConversation(CreateConversationRequest createConversationRequest, long userId){
        Conversation conversation = new Conversation();

        conversation.setUserId(userId);
        conversation.setTitle(createConversationRequest.title());
        conversation.setStatus(createConversationRequest.status());

        return conversationRepository.save(conversation);
    }

    public Conversation findByIdAndUserId(long id,  long userId){
        return conversationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ConversationNotFoundException("No conversation was found."));
    }

    public void deleteConversation(long id, long userId){
        conversationRepository.delete(findByIdAndUserId(id, userId));
    }

    public ConversationStatus alterStatus(long id, ConversationStatus conversationStatus , long userId){
        Conversation conversation = findByIdAndUserId(id, userId);

        conversation.setStatus(conversationStatus);

        Conversation conversationSaved = conversationRepository.save(conversation);

        return conversationSaved.getStatus();
    }

    public Conversation renameTitle(long id, AlterTitleRequest alterTitleNameRequest, long userId){
        Conversation conversation = findByIdAndUserId(id, userId);

        conversation.setTitle(alterTitleNameRequest.newTitle());

        return conversationRepository.save(conversation);
    }
}
