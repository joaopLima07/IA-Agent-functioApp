import { Sparkles } from "lucide-react";
import { cn } from "@/lib/utils";

export function AppLogo({ collapsed = false }: { collapsed?: boolean }) {
  return (
    <div className="flex items-center gap-2 overflow-hidden">
      <div className="flex size-7 shrink-0 items-center justify-center rounded-md bg-primary text-primary-foreground">
        <Sparkles className="size-4" />
      </div>
      <span
        className={cn(
          "truncate text-sm font-semibold tracking-tight transition-opacity",
          collapsed && "w-0 opacity-0"
        )}
      >
        Northwind AI
      </span>
    </div>
  );
}
