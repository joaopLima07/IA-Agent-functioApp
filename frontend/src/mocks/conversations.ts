import { subDays, subHours, subMinutes } from "date-fns";
import type { Conversation, Message } from "@/types";

const now = new Date();
const iso = (d: Date) => d.toISOString();

export const mockConversations: Conversation[] = [
  {
    id: "conv_1",
    title: "Order #49281 status",
    lastMessage: "Order #49281 is currently in transit.",
    agentId: "agt_customer_support",
    agentName: "Customer Support",
    status: "ACTIVE",
    createdAt: iso(subMinutes(now, 42)),
    updatedAt: iso(subMinutes(now, 40)),
    messageCount: 6,
  },
  {
    id: "conv_2",
    title: "Invoice request",
    lastMessage: "I've generated the second copy of invoice #18372.",
    agentId: "agt_finance",
    agentName: "Finance Assistant",
    status: "ACTIVE",
    createdAt: iso(subHours(now, 2)),
    updatedAt: iso(subHours(now, 2)),
    messageCount: 4,
    pinned: true,
  },
  {
    id: "conv_3",
    title: "Order support issue",
    lastMessage: "I've opened ticket #8843 for the warehouse team.",
    agentId: "agt_order_mgmt",
    agentName: "Order Management",
    status: "ACTIVE",
    createdAt: iso(subHours(now, 5)),
    updatedAt: iso(subHours(now, 5)),
    messageCount: 9,
  },
  {
    id: "conv_4",
    title: "Cancellation policy",
    lastMessage: "Orders can be cancelled within 24 hours of purchase.",
    agentId: "agt_customer_support",
    agentName: "Customer Support",
    status: "ACTIVE",
    createdAt: iso(subDays(now, 1)),
    updatedAt: iso(subDays(now, 1)),
    messageCount: 3,
  },
  {
    id: "conv_5",
    title: "Order #49102",
    lastMessage: "Your refund of $84.50 has been issued.",
    agentId: "agt_customer_support",
    agentName: "Customer Support",
    status: "ACTIVE",
    createdAt: iso(subDays(now, 1)),
    updatedAt: iso(subDays(now, 1)),
    messageCount: 7,
  },
  {
    id: "conv_6",
    title: "API rate limit error",
    lastMessage: "Filed bug report ENG-4471 for the engineering team.",
    agentId: "agt_tech_support",
    agentName: "Technical Support",
    status: "ACTIVE",
    createdAt: iso(subDays(now, 2)),
    updatedAt: iso(subDays(now, 2)),
    messageCount: 11,
  },
  {
    id: "conv_7",
    title: "AR aging report — July",
    lastMessage: "Total outstanding AR for July is $214,300.",
    agentId: "agt_finance",
    agentName: "Finance Assistant",
    status: "ACTIVE",
    createdAt: iso(subDays(now, 3)),
    updatedAt: iso(subDays(now, 3)),
    messageCount: 5,
  },
  {
    id: "conv_8",
    title: "SSO setup for Acme Corp",
    lastMessage: "SSO connection verified and active.",
    agentId: "agt_onboarding",
    agentName: "Onboarding Guide",
    status: "ARCHIVED",
    createdAt: iso(subDays(now, 6)),
    updatedAt: iso(subDays(now, 6)),
    messageCount: 8,
  },
  {
    id: "conv_9",
    title: "Refund policy question",
    lastMessage: "Refunds are processed within 5–7 business days.",
    agentId: "agt_customer_support",
    agentName: "Customer Support",
    status: "ARCHIVED",
    createdAt: iso(subDays(now, 9)),
    updatedAt: iso(subDays(now, 9)),
    messageCount: 4,
  },
];

export const mockMessagesByConversation: Record<string, Message[]> = {
  conv_1: [
    {
      id: "msg_1_1",
      conversationId: "conv_1",
      role: "USER",
      content: "What is the status of order #49281?",
      createdAt: iso(subMinutes(now, 42)),
    },
    {
      id: "msg_1_2",
      conversationId: "conv_1",
      role: "ASSISTANT",
      content: "Let me check that for you.",
      createdAt: iso(subMinutes(now, 41)),
      agentId: "agt_customer_support",
      agentName: "Customer Support",
      toolCalls: [
        {
          id: "tc_1",
          toolName: "checkOrder",
          label: "Checking order",
          status: "SUCCESS",
          parameters: { orderId: "49281" },
          result: { status: "IN_TRANSIT", carrier: "UPS", eta: "2026-08-16" },
          executionTimeMs: 1240,
          startedAt: iso(subMinutes(now, 41)),
          completedAt: iso(subMinutes(now, 41)),
        },
      ],
    },
    {
      id: "msg_1_3",
      conversationId: "conv_1",
      role: "ASSISTANT",
      content:
        "Order **#49281** is currently **in transit** via UPS and is expected to arrive by **August 16, 2026**.\n\nLet me know if you'd like the tracking link or want to make any changes to the delivery.",
      createdAt: iso(subMinutes(now, 40)),
      agentId: "agt_customer_support",
      agentName: "Customer Support",
    },
  ],
};

export function getConversationMessages(conversationId: string): Message[] {
  return mockMessagesByConversation[conversationId] ?? [];
}
