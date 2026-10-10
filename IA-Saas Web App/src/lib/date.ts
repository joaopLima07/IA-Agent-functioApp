import { isToday, isYesterday, format, formatDistanceToNow } from "date-fns";
import type { Conversation } from "@/types";

export function conversationDateGroup(dateStr: string): string {
  const date = new Date(dateStr);
  if (isToday(date)) return "Today";
  if (isYesterday(date)) return "Yesterday";
  const daysAgo = Math.floor((Date.now() - date.getTime()) / (1000 * 60 * 60 * 24));
  if (daysAgo < 7) return "Previous 7 days";
  if (daysAgo < 30) return "Previous 30 days";
  return format(date, "MMMM yyyy");
}

const GROUP_ORDER = ["Today", "Yesterday", "Previous 7 days", "Previous 30 days"];

export function groupConversationsByDate(conversations: Conversation[]): [string, Conversation[]][] {
  const groups = new Map<string, Conversation[]>();
  for (const conversation of conversations) {
    const key = conversationDateGroup(conversation.updatedAt);
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key)!.push(conversation);
  }
  const entries = Array.from(groups.entries());
  entries.sort(([a], [b]) => {
    const ai = GROUP_ORDER.indexOf(a);
    const bi = GROUP_ORDER.indexOf(b);
    if (ai !== -1 && bi !== -1) return ai - bi;
    if (ai !== -1) return -1;
    if (bi !== -1) return 1;
    return a < b ? 1 : -1;
  });
  return entries;
}

export function relativeTime(dateStr: string): string {
  return formatDistanceToNow(new Date(dateStr), { addSuffix: true });
}

export function formatDate(dateStr: string, pattern = "MMM d, yyyy"): string {
  return format(new Date(dateStr), pattern);
}

export function formatDateTime(dateStr: string): string {
  return format(new Date(dateStr), "MMM d, yyyy 'at' h:mm a");
}
