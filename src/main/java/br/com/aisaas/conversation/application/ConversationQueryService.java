package br.com.aisaas.conversation.application;

import br.com.aisaas.conversation.domain.Conversation;
import br.com.aisaas.conversation.domain.exceptions.ConversationNotFoundException;
import br.com.aisaas.conversation.infra.ConversationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationQueryService {
    private final ConversationRepository conversationRepository;

    public ConversationQueryService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public List<Conversation> listAllChats(long userId){
        return conversationRepository.findByUserId(userId);
    }

    public Conversation findById(long id, long userId){
        return conversationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ConversationNotFoundException("No conversation with this ID was found."));
    }

}
