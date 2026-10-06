import { mockOrganization, mockUser } from "@/mocks/user";
import { simulateLatency } from "@/services/config";
import type { AuthSession } from "@/types";

/**
 * Mock auth service shaped around the future Spring Security backend:
 * JWT access token + refresh token, with room for OAuth2 and RBAC. Swap
 * the bodies of these methods for real `apiClient` calls once
 * /auth/login, /auth/refresh, /auth/register exist — the interface and
 * every caller (AuthContext, route guards) stay the same.
 */

const SESSION_KEY = "auth_session";

function persistSession(session: AuthSession) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  localStorage.setItem("access_token", session.accessToken);
}

function buildSession(): AuthSession {
  const now = Date.now();
  return {
    accessToken: `mock.${btoa(JSON.stringify({ sub: mockUser.id, exp: now + 1000 * 60 * 30 }))}.signature`,
    refreshToken: `mock-refresh-${now}`,
    expiresAt: new Date(now + 1000 * 60 * 30).toISOString(),
    user: mockUser,
  };
}

export const authService = {
  async login(email: string, _password: string): Promise<AuthSession> {
    await simulateLatency(700);
    const session = buildSession();
    session.user = { ...mockUser, email: email || mockUser.email };
    persistSession(session);
    return session;
  },

  async register(name: string, email: string, _password: string): Promise<AuthSession> {
    await simulateLatency(800);
    const session = buildSession();
    session.user = { ...mockUser, name: name || mockUser.name, email: email || mockUser.email };
    persistSession(session);
    return session;
  },

  async forgotPassword(_email: string): Promise<{ sent: boolean }> {
    await simulateLatency(700);
    return { sent: true };
  },

  async logout(): Promise<void> {
    await simulateLatency(200);
    localStorage.removeItem(SESSION_KEY);
    localStorage.removeItem("access_token");
  },

  async refresh(): Promise<AuthSession | null> {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    await simulateLatency(200);
    const session = buildSession();
    persistSession(session);
    return session;
  },

  getStoredSession(): AuthSession | null {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as AuthSession;
    } catch {
      return null;
    }
  },

  getOrganization() {
    return mockOrganization;
  },
};
