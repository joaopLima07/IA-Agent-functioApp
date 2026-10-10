import { useMemo, useState } from "react";
import { MessageSquare, Search } from "lucide-react";
import { useConversations, useDeleteConversation, useArchiveConversation, useUnarchiveConversation } from "@/features/conversations/hooks/useConversations";
import { ConversationItem } from "./ConversationItem";
import { groupConversationsByDate } from "@/lib/date";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import type { Conversation } from "@/types";
import { Button } from "@/components/ui/button";

export function ConversationList() {
  const [query, setQuery] = useState("");
  const { data: conversations, isLoading, isError, refetch } = useConversations({ status: "ACTIVE" });
  const deleteConversation = useDeleteConversation();
  const archiveConversation = useArchiveConversation();
  const unarchiveConversation = useUnarchiveConversation();
  const [pendingDelete, setPendingDelete] = useState<Conversation | null>(null);

  const filtered = useMemo(() => {
    if (!conversations) return [];
    if (!query.trim()) return conversations;
    const q = query.toLowerCase();
    return conversations.filter(
      (c) => c.title.toLowerCase().includes(q) || c.lastMessage.toLowerCase().includes(q)
    );
  }, [conversations, query]);

  const grouped = useMemo(() => groupConversationsByDate(filtered), [filtered]);

  return (
    <div className="flex min-h-0 flex-1 flex-col">
      <div className="px-2 pb-2">
        <div className="relative">
          <Search className="pointer-events-none absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-muted-foreground" />
          <Input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search conversations"
            className="h-8 border-transparent bg-sidebar-accent/50 pl-8 text-xs shadow-none focus-visible:border-input"
            aria-label="Search conversations"
          />
        </div>
      </div>

      <div className="min-h-0 flex-1 overflow-y-auto px-2 scrollbar-thin">
        {isLoading && (
          <div className="space-y-2 px-1 py-1">
            {Array.from({ length: 6 }).map((_, i) => (
              <Skeleton key={i} className="h-10 w-full" />
            ))}
          </div>
        )}

        {isError && (
          <div className="px-1 py-4 text-center text-xs text-muted-foreground">
            <p className="mb-2">Couldn't load conversations.</p>
            <Button variant="outline" size="sm" onClick={() => refetch()}>
              Retry
            </Button>
          </div>
        )}

        {!isLoading && !isError && filtered.length === 0 && (
          <div className="flex flex-col items-center gap-2 px-2 py-10 text-center">
            <MessageSquare className="size-5 text-muted-foreground" />
            <p className="text-xs text-muted-foreground">
              {query ? "No conversations match your search." : "No conversations yet."}
            </p>
          </div>
        )}

        {!isLoading &&
          !isError &&
          grouped.map(([label, items]) => (
            <div key={label} className="mb-3">
              <p className="px-2.5 py-1.5 text-[11px] font-medium uppercase tracking-wide text-muted-foreground/80">
                {label}
              </p>
              <div className="space-y-0.5">
                {items.map((conversation) => (
                  <ConversationItem
                    key={conversation.id}
                    conversation={conversation}
                    onDelete={setPendingDelete}
                    onArchive={(c) => archiveConversation.mutate(c.id)}
                    onUnarchive={(c) => unarchiveConversation.mutate(c.id)}
                  />
                ))}
              </div>
            </div>
          ))}
      </div>

      <AlertDialog open={!!pendingDelete} onOpenChange={(open) => !open && setPendingDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete conversation?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently delete "{pendingDelete?.title}" and its message history. This action cannot be
              undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={() => {
                if (pendingDelete) deleteConversation.mutate(pendingDelete.id);
                setPendingDelete(null);
              }}
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
