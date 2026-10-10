import { Outlet } from "react-router-dom";
import { Sparkles } from "lucide-react";

export function AuthLayout() {
  return (
    <div className="flex min-h-svh w-full flex-col bg-secondary/30">
      <div className="flex flex-1 flex-col items-center justify-center px-4 py-10">
        <div className="mb-8 flex items-center gap-2">
          <div className="flex size-8 items-center justify-center rounded-md bg-primary text-primary-foreground">
            <Sparkles className="size-4" />
          </div>
          <span className="text-base font-semibold tracking-tight">Northwind AI</span>
        </div>
        <div className="w-full max-w-sm">
          <Outlet />
        </div>
      </div>
      <footer className="pb-6 text-center text-xs text-muted-foreground">
        © {new Date().getFullYear()} Northwind AI. Enterprise AI agent platform.
      </footer>
    </div>
  );
}
