package br.com.aisaas.conversation.application;

import br.com.aisaas.conversation.api.dto.AlterStatusConversationRequest;
import br.com.aisaas.conversation.api.dto.AlterTitleRequest;
import br.com.aisaas.conversation.api.dto.CreateConversationRequest;
import br.com.aisaas.conversation.domain.Conversation;
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

    public void alterStatus(AlterStatusConversationRequest alterStatusConversationRequest, long userId){
        Conversation conversation = findByIdAndUserId(alterStatusConversationRequest.id(), userId);

        conversation.setStatus(alterStatusConversationRequest.status());

        conversationRepository.save(conversation);
    }

    public void renameTitle(AlterTitleRequest alterTitleNameRequest, long userId){
        Conversation conversation = findByIdAndUserId(alterTitleNameRequest.id(), userId);

        conversation.setTitle(alterTitleNameRequest.newTitle());

        conversationRepository.save(conversation);
    }
}
