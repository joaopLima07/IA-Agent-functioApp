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
import br.com.aisaas.shared.api.dto.ErrorResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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


    public Map<Long, String> lastUserMessages(List<Long> conversationIds){
        if (conversationIds.isEmpty()){
            return Map.of();
        }

        return messageRepository.lastUserMessages(conversationIds).stream()
                .collect(Collectors.toMap(Message::getConversationId, Message::getContent, (a, b) -> a));
    }

    Conversation checkMessage(long id, long userId){
        return conversationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ConversationNotFoundException("This conversation could not be found for this user."));
    }

    List<org.springframework.ai.chat.messages.Message> toAiMessages(List<Message> history){
        List<org.springframework.ai.chat.messages.Message> result = new ArrayList<>();
        for (Message m : history){
            if (m.getRole() == MessageRole.USER){
                result.add(new UserMessage(m.getContent()));
            } else {
                result.add(new AssistantMessage(m.getContent()));
            }
        }

        return result;
    }

    long parseAgentId(String response){
        if (response == null || response.isBlank()){
            throw new InvalidRouterResponseException("The router returned an empty response.");
        }

        try {
            return Long.parseLong(response.trim());
        } catch (NumberFormatException e){
            throw new InvalidRouterResponseException("The router did not return a valid agent id: " + response);
        }
    }

    String listAllAgent(List<Agent> allAgents){
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

        return promptAgentsBuilder.toString();
    }

    String promptLastMessage(List<Message> lastMessage){
        String promptFiveMessage = "";

        for (Message message1 : lastMessage){
            promptFiveMessage += message1.getRole() + ": " + message1.getContent() + " | AgentId: " + message1.getAgentId() + "\n";
        }

        return promptFiveMessage;
    }

    public SseEmitter orquestratorMessage(long id, SendMessageRequest sendMessageRequest, long userId){
        long start = System.nanoTime();

        Conversation conversation = checkMessage(id, userId);

        Message message = new Message();

        message.setConversationId(conversation.getId());
        message.setRole(MessageRole.USER);
        message.setContent(sendMessageRequest.content());

        messageRepository.save(message);

        List<Message> last5Message = lastMessages(message.getConversationId(), 5);

        String promptAgents = listAllAgent(agentQueryService.searchAll());

        String promptFiveMessage = promptLastMessage(last5Message);

        Agent routerAgent = agentQueryService.findRouterAgent();

        ChatResponse agentRouterResponse = agentRouter.choseAgent(routerAgent, promptFiveMessage, promptAgents);

        long agentRouterId = parseAgentId(agentRouterResponse.getResult().getOutput().getText());

        Usage usageRouterResponse = agentRouterResponse.getMetadata().getUsage();


        int inputTokens = usageRouterResponse.getPromptTokens();

        int outputTokens = usageRouterResponse.getCompletionTokens();

        int totalTokens = usageRouterResponse.getTotalTokens();

        Agent chosenAgent = agentQueryService.searchUnique(agentRouterId);



        StringBuilder fullText = new StringBuilder();
        AtomicReference<Usage> lastUsage = new AtomicReference<>();

        List<org.springframework.ai.chat.messages.Message> toAiMessage = toAiMessages(last5Message);

        SseEmitter emitter = new SseEmitter(120_000L);

        replyGeneretor.finalMessage(chosenAgent, toAiMessage)
                .doOnNext(chunk -> {
                    // o usage normalmente só vem preenchido nos últimos chunks
                    if (chunk.getMetadata() != null && chunk.getMetadata().getUsage() != null) {
                        lastUsage.set(chunk.getMetadata().getUsage());
                    }
                })
                .mapNotNull(chunk -> chunk.getResult() == null ? null : chunk.getResult().getOutput().getText())
                .doOnNext(fullText::append)
                .doFinally(signal -> {
                    // roda em complete, erro e cancel (cliente desconectou): salva a resposta parcial também
                    if (fullText.isEmpty()) {
                        return;
                    }

                    int latencyMs = (int) ((System.nanoTime() - start) / 1_000_000);
                    Usage usage = lastUsage.get();

                    Message aiMessage = new Message();

                    aiMessage.setContent(fullText.toString());
                    aiMessage.setAgentId(chosenAgent.getId());
                    aiMessage.setRole(MessageRole.ASSISTANT);
                    aiMessage.setConversationId(conversation.getId());
                    aiMessage.setLatencyMs(latencyMs);

                    if (usage != null) {
                        aiMessage.setInputToken(usage.getPromptTokens() + inputTokens);
                        aiMessage.setOutputToken(usage.getCompletionTokens() + outputTokens);
                        aiMessage.setTotalToken(usage.getTotalTokens() + totalTokens);
                    }

                    messageRepository.save(aiMessage);
                })
                // sem isso o stream pode rodar na thread da requisição: os envios feitos antes do
                // controller retornar ficam em buffer no SseEmitter e saem todos de uma vez
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        text -> {
                            try {
                                emitter.send(text);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            // o advice não alcança erros do stream: manda um evento "error" antes de fechar
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data(new ErrorResponse("TECHNICAL_ERROR", error.getMessage())));
                            } catch (IOException ignored) {
                                // cliente já desconectou
                            }
                            emitter.complete();
                        },
                        emitter::complete
                );

        return emitter;

        // Primeiro subir apenas os Agents de IA sem utilizar o tool caling e alterando dados do banco
        // depois tentar adicionar para chamar o toolCalling


    }
}
