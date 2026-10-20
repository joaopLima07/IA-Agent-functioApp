package br.com.aisaas.conversation.application;

import br.com.aisaas.agent.application.AgentQueryService;
import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.conversation.api.dto.SendMessageRequest;
import br.com.aisaas.conversation.application.dto.ReplyResult;
import br.com.aisaas.conversation.domain.Conversation;
import br.com.aisaas.conversation.domain.Message;
import br.com.aisaas.conversation.domain.MessageRole;
import br.com.aisaas.conversation.domain.exceptions.ConversationNotFoundException;
import br.com.aisaas.conversation.infra.ConversationRepository;
import br.com.aisaas.conversation.infra.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ChatService {


    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final AgentQueryService agentQueryService;
    private final AgentRouter agentRouter;
    private final ReplyGeneretor replyGeneretor;


    public ChatService(MessageRepository messageRepository, ConversationRepository conversationRepository, AgentQueryService agentQueryService, ReplyGeneretor replyGeneretor, AgentRouter agentRouter) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.agentQueryService = agentQueryService;
        this.agentRouter = agentRouter;
        this.replyGeneretor = replyGeneretor;
    }


    public List<Message> lastMessages(long conversationId, int pageSize){
        Pageable limit = PageRequest.of(0, pageSize);

        return messageRepository.searchLastMessages(conversationId, limit).stream()
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .toList();
    }

    public List<Message> lastMessagesBefore(long id, long messageId){
        Pageable pageable = PageRequest.of(0, 30);

        return messageRepository.searchBeforeMessages(id, messageId, pageable).stream()
                .sorted(Comparator.comparing(Message :: getCreatedAt))
                .toList();
    }


    Conversation checkMessage(SendMessageRequest sendMessageRequest, long userId){
        return conversationRepository.findByIdAndUserId(sendMessageRequest.id(), userId)
                .orElseThrow(() -> new ConversationNotFoundException("This conversation could not be found for this user."));
    }

    public String orquestratorMessage(SendMessageRequest sendMessageRequest, long userId){
        Conversation conversation = checkMessage(sendMessageRequest, userId);

        Message message = new Message();

        message.setConversationId(conversation.getId());
        message.setRole(MessageRole.USER);
        message.setContent(sendMessageRequest.content());

        messageRepository.save(message);

        List<Message> last5Message = lastMessages(message.getConversationId(), 5);

        List<Agent> allAgents = agentQueryService.searchAll();


        StringBuilder promptAgentsBuilder = new StringBuilder();

        for (Agent allAgent : allAgents){
            promptAgentsBuilder.append("id: ")
                    .append(allAgent.getId())
                    .append(" | nome: ")
                    .append(allAgent.getName())
                    .append(" | descrição: ")
                    .append(allAgent.getDescription())
                    .append("\n");
        }

        String promptAgents = promptAgentsBuilder.toString();


        String promptFiveMessage = "";

        for (Message message1 : last5Message){
            promptFiveMessage += message1.getRole() + ": " + message1.getContent() + " | AgentId: " + message1.getAgentId() + "\n";
        }


        Agent routerAgent = agentQueryService.findRouterAgent();

        long agentRouterResponse = agentRouter.choseAgent(routerAgent, promptFiveMessage, promptAgents);

        Agent chosenAgent = agentQueryService.searchUnique(agentRouterResponse);

        ReplyResult finalMesage = replyGeneretor.finalMessage(chosenAgent, promptFiveMessage);

        Message aiMessage = new Message();

        aiMessage.setContent(finalMesage.content());
        aiMessage.setAgentId(chosenAgent.getId());
        aiMessage.setRole(MessageRole.ASSISTANT);
        aiMessage.setConversationId(conversation.getId());
        aiMessage.setInputToken(finalMesage.inputToken());
        aiMessage.setOutputToken(finalMesage.outputToken());
        aiMessage.setLatencyMs(finalMesage.latencyMs());

        messageRepository.save(aiMessage);

        return finalMesage.content();

        // Primeiro subir apenas os Agents de IA sem utilizar o tool caling e alterando dados do banco
        // depois tentar adicionar para chamar o toolCalling


    }
}
