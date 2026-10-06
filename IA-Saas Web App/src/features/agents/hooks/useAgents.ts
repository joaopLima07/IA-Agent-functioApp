import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { agentService } from "@/features/agents/services/agentService";
import type { Agent } from "@/types";
import { toast } from "sonner";

export const agentKeys = {
  all: ["agents"] as const,
  detail: (id: string) => ["agents", "detail", id] as const,
};

export function useAgents() {
  return useQuery({ queryKey: agentKeys.all, queryFn: () => agentService.getAll() });
}

export function useAgent(id: string | undefined) {
  return useQuery({
    queryKey: agentKeys.detail(id ?? ""),
    queryFn: () => agentService.getById(id!),
    enabled: !!id,
  });
}

export function useUpdateAgent(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (patch: Partial<Agent>) => agentService.update(id, patch),
    onSuccess: (updated) => {
      queryClient.setQueryData(agentKeys.detail(id), updated);
      queryClient.invalidateQueries({ queryKey: agentKeys.all });
      toast.success("Agent updated");
    },
    onError: () => toast.error("Couldn't save changes", { description: "Please try again." }),
  });
}
