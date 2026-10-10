import { mockAgents } from "@/mocks/agents";
import { simulateLatency } from "@/services/config";
import type { Agent } from "@/types";

let agents: Agent[] = [...mockAgents];

export const agentService = {
  async getAll(): Promise<Agent[]> {
    await simulateLatency();
    return agents;
  },

  async getById(id: string): Promise<Agent | undefined> {
    await simulateLatency(300);
    return agents.find((a) => a.id === id);
  },

  async update(id: string, patch: Partial<Agent>): Promise<Agent> {
    await simulateLatency(400);
    let updated: Agent | undefined;
    agents = agents.map((a) => {
      if (a.id === id) {
        updated = { ...a, ...patch, updatedAt: new Date().toISOString() };
        return updated;
      }
      return a;
    });
    if (!updated) throw new Error("Agente não encontrado");
    return updated;
  },
};
