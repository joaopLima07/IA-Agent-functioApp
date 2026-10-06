import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { knowledgeService } from "@/features/knowledge/services/knowledgeService";
import { toast } from "sonner";

export const knowledgeKeys = {
  all: ["knowledge-documents"] as const,
};

export function useDocuments() {
  return useQuery({
    queryKey: knowledgeKeys.all,
    queryFn: () => knowledgeService.getAll(),
    refetchInterval: (query) => (query.state.data?.some((d) => d.status === "PROCESSING") ? 2500 : false),
  });
}

export function useUploadDocument() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (file: { name: string; size: number }) => knowledgeService.upload(file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: knowledgeKeys.all });
      toast.success("Document uploaded", { description: "Processing has started." });
    },
    onError: () => toast.error("Upload failed", { description: "Please try again." }),
  });
}

export function useDeleteDocument() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => knowledgeService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: knowledgeKeys.all });
      toast.success("Document deleted");
    },
  });
}

export function useReprocessDocument() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => knowledgeService.reprocess(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: knowledgeKeys.all });
      toast.success("Reprocessing started");
    },
  });
}
