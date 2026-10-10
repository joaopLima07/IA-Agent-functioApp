import { Sparkles } from "lucide-react";
import { MessageInput } from "./MessageInput";

interface ChatEmptyStateProps {
  onSend: (content: string) => void;
}

const suggestions = [
  "Check order #49281",
  "Check customer account status",
  "Generate invoice #18372",
  "Open a support ticket",
];

export function ChatEmptyState({ onSend }: ChatEmptyStateProps) {
  return (
    <div className="flex h-full flex-col items-center justify-center px-4">
      <div className="flex w-full max-w-xl flex-col items-center text-center">
        <div className="mb-4 flex size-11 items-center justify-center rounded-xl bg-primary/10 text-primary">
          <Sparkles className="size-5" />
        </div>
        <h1 className="text-xl font-semibold tracking-tight text-foreground sm:text-2xl">How can I help?</h1>
        <p className="mt-1.5 text-sm text-muted-foreground">Interact with your AI agents</p>

        <div className="mt-6 w-full">
          <MessageInput onSend={onSend} isStreaming={false} onStop={() => {}} autoFocus />
        </div>

        <div className="mt-4 flex flex-wrap items-center justify-center gap-2">
          {suggestions.map((s) => (
            <button
              key={s}
              type="button"
              onClick={() => onSend(s)}
              className="rounded-full border border-border bg-card px-3.5 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:border-primary/40 hover:text-foreground"
            >
              {s}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
