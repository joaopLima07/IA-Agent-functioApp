export type UserRole = "OWNER" | "ADMIN" | "MEMBER" | "VIEWER";

export interface User {
  id: string;
  name: string;
  email: string;
  avatarUrl?: string;
  role: UserRole;
  organizationId: string;
  createdAt: string;
}

export interface Organization {
  id: string;
  name: string;
  logoUrl?: string;
  plan: "STARTER" | "GROWTH" | "ENTERPRISE";
}

/**
 * Auth session shape the future Spring Boot backend will issue.
 * Designed up front for JWT access + refresh token rotation.
 */
export interface AuthSession {
  accessToken: string;
  refreshToken: string;
  expiresAt: string;
  user: User;
}

export type Permission =
  | "conversations:read"
  | "conversations:write"
  | "agents:read"
  | "agents:write"
  | "knowledge:read"
  | "knowledge:write"
  | "settings:write"
  | "org:admin";

export const ROLE_PERMISSIONS: Record<UserRole, Permission[]> = {
  OWNER: [
    "conversations:read",
    "conversations:write",
    "agents:read",
    "agents:write",
    "knowledge:read",
    "knowledge:write",
    "settings:write",
    "org:admin",
  ],
  ADMIN: [
    "conversations:read",
    "conversations:write",
    "agents:read",
    "agents:write",
    "knowledge:read",
    "knowledge:write",
    "settings:write",
  ],
  MEMBER: ["conversations:read", "conversations:write", "agents:read", "knowledge:read"],
  VIEWER: ["conversations:read", "agents:read", "knowledge:read"],
};
