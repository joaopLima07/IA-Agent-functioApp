import { subDays, format } from "date-fns";
import type { DashboardData } from "@/types";

const now = new Date();

function lastNDays(n: number) {
  return Array.from({ length: n }, (_, i) => subDays(now, n - 1 - i));
}

export const mockDashboardData: DashboardData = {
  summary: {
    totalConversations: 1284,
    conversationsDelta: 8.2,
    totalMessages: 8421,
    messagesDelta: 5.6,
    resolutionRate: 78,
    resolutionDelta: 2.4,
    totalTokens: 1_204_500,
    tokensDelta: 12.1,
  },
  conversationsOverTime: lastNDays(14).map((d, i) => ({
    date: format(d, "MMM d"),
    value: Math.round(60 + Math.sin(i / 2) * 18 + i * 2.4 + (i % 4 === 0 ? 12 : 0)),
  })),
  agentUsage: [
    { agentName: "Customer Support", conversations: 512 },
    { agentName: "Technical Support", conversations: 341 },
    { agentName: "Order Management", conversations: 218 },
    { agentName: "Finance Assistant", conversations: 156 },
    { agentName: "Invoice Assistant", conversations: 57 },
  ],
  toolOutcomes: lastNDays(7).map((d, i) => ({
    date: format(d, "EEE"),
    success: Math.round(140 + i * 6),
    failed: Math.round(8 + (i % 3)),
  })),
  tokenUsage: lastNDays(14).map((d, i) => ({
    date: format(d, "MMM d"),
    value: Math.round(65000 + i * 3200 + Math.cos(i / 3) * 8000),
  })),
  responseTime: lastNDays(7).map((d, i) => ({
    date: format(d, "EEE"),
    avgSeconds: Number((1.8 + Math.sin(i) * 0.4 + 0.05 * i).toFixed(2)),
  })),
};
