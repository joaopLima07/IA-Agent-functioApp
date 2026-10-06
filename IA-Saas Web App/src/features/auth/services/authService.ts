import { mockOrganization } from "@/mocks/user";
import { apiClient } from "@/services/api";
import { API_CONFIG, simulateLatency } from "@/services/config";
import { ApiRequestError } from "@/types/api";
import type { AuthSession, User, UserRole } from "@/types";

/**
 * Auth service backed by the Spring Boot `/api/auth/*` endpoints
 * (register, login, me). The backend issues only a short-lived access
 * token — there is no refresh endpoint — so an expired session simply
 * sends the user back to the login page.
 */

const SESSION_KEY = "auth_session";
const TOKEN_KEY = "access_token";

const authApi = { baseUrl: API_CONFIG.authBaseUrl };

interface LoginResponse {
  accessToken: string;
  expiresIn: number;
}

interface MeResponse {
  id: number;
  fullName: string;
  email: string;
  phone?: string;
  role: UserRole;
}

function toUser(me: MeResponse): User {
  return {
    id: String(me.id),
    name: me.fullName,
    email: me.email,
    phone: me.phone,
    role: me.role,
  };
}

function persistSession(session: AuthSession) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  localStorage.setItem(TOKEN_KEY, session.accessToken);
}

function clearSession() {
  localStorage.removeItem(SESSION_KEY);
  localStorage.removeItem(TOKEN_KEY);
}

async function login(email: string, password: string): Promise<AuthSession> {
  const { accessToken, expiresIn } = await apiClient.post<LoginResponse>(
    "/login",
    { email, password },
    { ...authApi, auth: false },
  );
  // /me needs the token, which apiClient reads from storage.
  localStorage.setItem(TOKEN_KEY, accessToken);
  try {
    const me = await apiClient.get<MeResponse>("/me", authApi);
    const session: AuthSession = {
      accessToken,
      expiresAt: new Date(Date.now() + expiresIn * 1000).toISOString(),
      user: toUser(me),
    };
    persistSession(session);
    return session;
  } catch (error) {
    clearSession();
    throw error;
  }
}

export const authService = {
  login,

  /** Registers the user, then signs in (the backend returns no token on register). */
  async register(name: string, email: string, password: string, phone: string): Promise<AuthSession> {
    await apiClient.post("/register", { fullName: name, email, password, phone }, { ...authApi, auth: false });
    return login(email, password);
  },

  /** No backend endpoint yet — still mocked. */
  async forgotPassword(_email: string): Promise<{ sent: boolean }> {
    await simulateLatency(700);
    return { sent: true };
  },

  async logout(): Promise<void> {
    clearSession();
  },

  /** Re-fetches the current user. Returns null (and clears the session) if the token was rejected. */
  async fetchCurrentUser(): Promise<User | null> {
    try {
      return toUser(await apiClient.get<MeResponse>("/me", authApi));
    } catch (error) {
      if (error instanceof ApiRequestError && error.status === 401) {
        clearSession();
        return null;
      }
      throw error;
    }
  },

  getStoredSession(): AuthSession | null {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try {
      const session = JSON.parse(raw) as AuthSession;
      if (new Date(session.expiresAt).getTime() <= Date.now()) {
        clearSession();
        return null;
      }
      return session;
    } catch {
      return null;
    }
  },

  /** No backend endpoint yet — still mocked. */
  getOrganization() {
    return mockOrganization;
  },
};
