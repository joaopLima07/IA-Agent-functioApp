import { MessageSquare, MessagesSquare, CheckCircle2, Coins } from "lucide-react";
import { useDashboardData } from "@/features/dashboard/hooks/useDashboard";
import { StatTile } from "@/features/dashboard/components/StatTile";
import { ConversationsOverTimeChart } from "@/features/dashboard/components/ConversationsOverTimeChart";
import { AgentUsageChart } from "@/features/dashboard/components/AgentUsageChart";
import { ToolOutcomesChart } from "@/features/dashboard/components/ToolOutcomesChart";
import { TokenUsageChart } from "@/features/dashboard/components/TokenUsageChart";
import { ResponseTimeChart } from "@/features/dashboard/components/ResponseTimeChart";
import { PageHeader } from "@/components/common/PageHeader";
import { ErrorState } from "@/components/common/ErrorState";
import { Skeleton } from "@/components/ui/skeleton";
import { formatCompactNumber, formatNumber } from "@/lib/format";

export default function DashboardPage() {
  const { data, isLoading, isError, refetch } = useDashboardData();

  return (
    <div className="h-full overflow-y-auto scrollbar-thin">
      <div className="mx-auto max-w-6xl space-y-6 px-4 py-6 sm:px-6 lg:px-8">
        <PageHeader title="Dashboard" description="Platform-wide performance across all agents and conversations." />

        {isError && <ErrorState description="Couldn't load dashboard metrics." onRetry={() => refetch()} />}

        {isLoading && (
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
            {Array.from({ length: 4 }).map((_, i) => (
              <Skeleton key={i} className="h-28 w-full" />
            ))}
          </div>
        )}

        {data && (
          <>
            <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
              <StatTile
                label="Conversations"
                value={formatNumber(data.summary.totalConversations)}
                delta={data.summary.conversationsDelta}
                icon={MessageSquare}
              />
              <StatTile
                label="Messages"
                value={formatNumber(data.summary.totalMessages)}
                delta={data.summary.messagesDelta}
                icon={MessagesSquare}
              />
              <StatTile
                label="Resolution rate"
                value={`${data.summary.resolutionRate}%`}
                delta={data.summary.resolutionDelta}
                icon={CheckCircle2}
              />
              <StatTile
                label="Tokens used"
                value={formatCompactNumber(data.summary.totalTokens)}
                delta={data.summary.tokensDelta}
                icon={Coins}
              />
            </div>

            <div className="grid gap-4 lg:grid-cols-2">
              <ConversationsOverTimeChart data={data.conversationsOverTime} />
              <AgentUsageChart data={data.agentUsage} />
              <ToolOutcomesChart data={data.toolOutcomes} />
              <TokenUsageChart data={data.tokenUsage} />
              <div className="lg:col-span-2">
                <ResponseTimeChart data={data.responseTime} />
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
