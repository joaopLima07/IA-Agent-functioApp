import { Bell } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Badge } from "@/components/ui/badge";

const notifications = [
  {
    id: "n1",
    title: "Document processing failed",
    description: "\"FAQ\" could not be processed. Review the file and re-upload.",
    time: "6h ago",
  },
  {
    id: "n2",
    title: "New agent published",
    description: "Order Management is now active for your organization.",
    time: "1d ago",
  },
  {
    id: "n3",
    title: "Weekly usage summary",
    description: "1,284 conversations handled this week, up 8.2%.",
    time: "2d ago",
  },
];

export function NotificationsMenu() {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon" className="relative" aria-label="Notifications">
          <Bell className="size-4" />
          <span className="absolute right-1.5 top-1.5 size-1.5 rounded-full bg-primary" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-80">
        <DropdownMenuLabel className="flex items-center justify-between text-foreground">
          Notifications
          <Badge variant="secondary">{notifications.length} new</Badge>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        {notifications.map((n) => (
          <DropdownMenuItem key={n.id} className="flex flex-col items-start gap-0.5 whitespace-normal py-2">
            <span className="text-sm font-medium">{n.title}</span>
            <span className="text-xs text-muted-foreground">{n.description}</span>
            <span className="mt-0.5 text-[11px] text-muted-foreground/70">{n.time}</span>
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
