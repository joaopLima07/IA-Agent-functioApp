import { Link } from "react-router-dom";
import { Compass } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function NotFoundPage() {
  return (
    <div className="flex h-svh w-full flex-col items-center justify-center gap-3 px-4 text-center">
      <div className="flex size-11 items-center justify-center rounded-xl bg-secondary text-muted-foreground">
        <Compass className="size-5" />
      </div>
      <h1 className="text-lg font-semibold">Page not found</h1>
      <p className="max-w-sm text-sm text-muted-foreground">The page you're looking for doesn't exist or may have been moved.</p>
      <Button asChild className="mt-2">
        <Link to="/">Back to chat</Link>
      </Button>
    </div>
  );
}
