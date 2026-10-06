export interface ScenarioToolCall {
  tool: string;
  label: string;
  parameters: Record<string, unknown>;
  result?: Record<string, unknown> | string;
  errorMessage?: string;
  durationMs: number;
  outcome: "SUCCESS" | "ERROR";
}

export interface Scenario {
  match: RegExp;
  agentId: string;
  toolCalls: ScenarioToolCall[];
  response: string;
}

export const scenarios: Scenario[] = [
  {
    match: /order.*#?(\d{4,6})|status of (my )?order|track(ing)? (my )?order/i,
    agentId: "agt_customer_support",
    toolCalls: [
      {
        tool: "checkOrder",
        label: "Checking order",
        parameters: { orderId: "49281" },
        result: { status: "IN_TRANSIT", carrier: "UPS", eta: "2026-08-16" },
        durationMs: 1240,
        outcome: "SUCCESS",
      },
    ],
    response:
      "Order **#49281** is currently **in transit** via UPS and is expected to arrive by **August 16, 2026**.\n\nLet me know if you'd like the tracking link or want to make any changes to the delivery.",
  },
  {
    match: /invoice|second copy|billing statement/i,
    agentId: "agt_finance",
    toolCalls: [
      {
        tool: "generateInvoiceCopy",
        label: "Generating invoice copy",
        parameters: { invoiceId: "18372", format: "PDF" },
        result: { invoiceId: "18372", total: "$4,280.00", status: "GENERATED" },
        durationMs: 1860,
        outcome: "SUCCESS",
      },
    ],
    response:
      "I've generated a copy of **invoice #18372** for **$4,280.00**.\n\n| Field | Value |\n|---|---|\n| Invoice | #18372 |\n| Issued | Jul 28, 2026 |\n| Due | Aug 27, 2026 |\n| Status | Paid |\n\nI can email this to the billing contact on file — just confirm the address.",
  },
  {
    match: /refund|cancellation policy|cancel.*order|return policy/i,
    agentId: "agt_customer_support",
    toolCalls: [
      {
        tool: "checkPolicy",
        label: "Looking up policy",
        parameters: { topic: "refunds_and_cancellations" },
        result: { source: "Return & Refund Policy", lastUpdated: "2026-05-02" },
        durationMs: 890,
        outcome: "SUCCESS",
      },
    ],
    response:
      "Here's a summary of our refund and cancellation policy:\n\n- Orders can be **cancelled free of charge within 24 hours** of purchase.\n- Refunds for returned items are processed within **5–7 business days** after we receive the item.\n- Digital products and gift cards are **non-refundable**.\n\nWant me to start a cancellation or refund for a specific order?",
  },
  {
    match: /bug|error|not working|crash|fail(ing)?|500 error|rate limit/i,
    agentId: "agt_tech_support",
    toolCalls: [
      {
        tool: "runDiagnostics",
        label: "Running diagnostics",
        parameters: { workspace: "northwind-prod", scope: "api" },
        result: { errorRate: "2.4%", topError: "429 Too Many Requests", affectedEndpoint: "/v1/orders" },
        durationMs: 2100,
        outcome: "SUCCESS",
      },
      {
        tool: "fileBugReport",
        label: "Filing bug report",
        parameters: { title: "Elevated 429s on /v1/orders", severity: "P2" },
        result: { ticketId: "ENG-4471", status: "OPEN" },
        durationMs: 760,
        outcome: "SUCCESS",
      },
    ],
    response:
      "I found an elevated rate of `429 Too Many Requests` errors on `/v1/orders` (2.4% of requests in the last hour). This looks like the client-side retry isn't backing off correctly.\n\n```ts\n// Suggested fix: exponential backoff with jitter\nasync function withBackoff(fn: () => Promise<Response>, attempt = 0): Promise<Response> {\n  const res = await fn();\n  if (res.status === 429 && attempt < 5) {\n    const delay = Math.min(1000 * 2 ** attempt, 8000) + Math.random() * 250;\n    await new Promise((r) => setTimeout(r, delay));\n    return withBackoff(fn, attempt + 1);\n  }\n  return res;\n}\n```\n\nI've filed **ENG-4471** for the engineering team to add server-side rate-limit headers. I'll keep you posted here once it's resolved.",
  },
  {
    match: /support ticket|open a ticket|talk to a human|escalate/i,
    agentId: "agt_customer_support",
    toolCalls: [
      {
        tool: "openSupportTicket",
        label: "Opening support ticket",
        parameters: { priority: "NORMAL", category: "general" },
        result: { ticketId: "8843", queue: "Customer Support" },
        durationMs: 640,
        outcome: "SUCCESS",
      },
    ],
    response:
      "I've opened **ticket #8843** and routed it to our support queue. A team member will follow up by email within one business day.\n\nIs there anything else I can help you with in the meantime?",
  },
  {
    match: /customer|check.*(email|account|profile)/i,
    agentId: "agt_customer_support",
    toolCalls: [
      {
        tool: "checkCustomer",
        label: "Checking customer",
        parameters: { email: "customer@example.com" },
        result: { accountStatus: "ACTIVE", tier: "Gold", ordersLast90d: 4 },
        durationMs: 980,
        outcome: "SUCCESS",
      },
    ],
    response:
      "This customer's account is **active** with **Gold tier** status and 4 orders placed in the last 90 days. Let me know what you'd like to look into next.",
  },
];

export const genericAssistantResponses = [
  "I can help with that. Could you share a bit more detail — an order number, invoice ID, or account email — so I can look up the right record?",
  "Got it. I can check order status, generate invoice copies, look up policies, or open a support ticket. What would you like me to do?",
  "I don't have enough context to act on that yet. Could you clarify what you'd like me to check or do?",
];
