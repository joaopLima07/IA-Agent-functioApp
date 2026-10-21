package br.com.aisaas.panel.infra;

import br.com.aisaas.conversation.domain.Message;
import br.com.aisaas.panel.api.dto.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PanelRepository extends JpaRepository<Message, Long> {

    @Query("""
            select new br.com.aisaas.panel.api.dto.DailyTokens(
                cast(m.createdAt as LocalDate), sum(m.totalToken))
            from Message m
            group by cast(m.createdAt as LocalDate)
            order by cast(m.createdAt as LocalDate)
            """)
    List<DailyTokens> dailyToken();

    @Query("""
            select new br.com.aisaas.panel.api.dto.ConversationsCreated(
                cast(c.createdAt as LocalDate), count(c.id))
            from Conversation c
            where cast(c.createdAt as LocalDate) >= local_date - 14 day
            group by cast(c.createdAt as LocalDate)
            order by cast(c.createdAt as LocalDate)
            """)
    List<ConversationsCreated> convesationsCreated();

    @Query("""
            select new br.com.aisaas.panel.api.dto.CardsResponse(
                (select count(c) from Conversation c),
                count(m),
                coalesce(sum(m.totalToken), 0L))
            from Message m
            """)
    CardsResponse cards();

    @Query("""
            select new br.com.aisaas.panel.api.dto.AgentUsageResponse(
                cast(m.createdAt as LocalDate), m.agentId, count(m.agentId))
            from Message m
            where m.agentId <> 0
            group by m.agentId, cast(m.createdAt as LocalDate)
            order by cast(m.createdAt as LocalDate), m.agentId
            """)
    List<AgentUsageResponse> agentUsage();

    @Query("""
            select new br.com.aisaas.panel.api.dto.ResponseTimeResponse(
                cast(m.createdAt as LocalDate), avg(m.latencyMs))
            from Message m
            where m.role = br.com.aisaas.conversation.domain.MessageRole.ASSISTANT
            group by cast(m.createdAt as LocalDate)
            order by cast(m.createdAt as LocalDate)
            """)
    List<ResponseTimeResponse> responseTime();
}
