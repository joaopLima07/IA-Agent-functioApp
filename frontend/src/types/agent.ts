export type AgentStatus = "ACTIVE" | "INACTIVE" | "DRAFT";

export type AiProvider = "OpenAI" | "Anthropic" | "Gemini";

export interface AgentTool {
  id: string;
  name: string;
  description: string;
  category: string;
  enabled: boolean;
}

export interface AgentKnowledgeSource {
  id: string;
  name: string;
  documentCount: number;
}

export interface AgentPermission {
  id: string;
  label: string;
  description: string;
  granted: boolean;
}

export interface Agent {
  id: string;
  name: string;
  description: string;
  status: AgentStatus;
  provider: AiProvider;
  model: string;
  toolCount: number;
  documentCount: number;
  updatedAt: string;
  createdAt: string;
  conversationsHandled: number;
  avatarColor: string;
  instructions: string;
  temperature: number;
  maxTokens: number;
  tools: AgentTool[];
  knowledgeSources: AgentKnowledgeSource[];
  permissions: AgentPermission[];
}
