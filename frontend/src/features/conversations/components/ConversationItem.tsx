import { NavLink } from "react-router-dom";
import { Archive, ArchiveRestore, MoreHorizontal, Trash2 } from "lucide-react";
import { cn } from "@/lib/utils";
import type { Conversation } from "@/types";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";

interface ConversationItemProps {
  conversation: Conversation;
  onDelete: (conversation: Conversation) => void;
  onArchive: (conversation: Conversation) => void;
  onUnarchive: (conversation: Conversation) => void;
}

export function ConversationItem({ conversation, onDelete, onArchive, onUnarchive }: ConversationItemProps) {
  return (
    <NavLink
      to={`/c/${conversation.id}`}
      className={({ isActive }) =>
        cn(
          "group relative flex items-center gap-2 rounded-md px-2.5 py-2 text-sm transition-colors",
          isActive
            ? "bg-sidebar-accent text-sidebar-accent-foreground"
            : "text-sidebar-foreground/80 hover:bg-sidebar-accent/60 hover:text-sidebar-accent-foreground"
        )
      }
    >
      <div className="min-w-0 flex-1">
        <p className="truncate font-medium leading-tight">{conversation.title}</p>
        <p className="truncate text-xs text-muted-foreground">{conversation.lastMessage || "No messages yet"}</p>
      </div>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button
            variant="ghost"
            size="icon-sm"
            className="shrink-0 opacity-0 group-hover:opacity-100 group-focus-within:opacity-100 data-[state=open]:opacity-100"
            onClick={(e) => e.preventDefault()}
            aria-label={`Conversation options for ${conversation.title}`}
          >
            <MoreHorizontal className="size-4" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" onClick={(e) => e.stopPropagation()}>
          {conversation.status === "ACTIVE" ? (
            <DropdownMenuItem
              onSelect={(e) => {
                e.preventDefault();
                onArchive(conversation);
              }}
            >
              <Archive className="size-4" />
              Archive
            </DropdownMenuItem>
          ) : (
            <DropdownMenuItem
              onSelect={(e) => {
                e.preventDefault();
                onUnarchive(conversation);
              }}
            >
              <ArchiveRestore className="size-4" />
              Move to active
            </DropdownMenuItem>
          )}
          <DropdownMenuItem
            variant="destructive"
            onSelect={(e) => {
              e.preventDefault();
              onDelete(conversation);
            }}
          >
            <Trash2 className="size-4" />
            Delete
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </NavLink>
  );
}
