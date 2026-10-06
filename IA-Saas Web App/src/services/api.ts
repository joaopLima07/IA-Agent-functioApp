import { API_CONFIG } from "./config";
import { ApiRequestError, type ApiError } from "@/types/api";

/**
 * Thin fetch wrapper matching the shape the Spring Boot backend will use:
 * JSON in/out, bearer auth header, and a typed error channel. Mock
 * services do not use this directly (they resolve in-memory data), but
 * real HTTP-backed services should be implemented against this client so
 * only `services/api.ts` needs to change when the backend goes live.
 */

interface RequestOptions extends RequestInit {
  auth?: boolean;
  baseUrl?: string;
}

function getAccessToken(): string | null {
  try {
    return localStorage.getItem("access_token");
  } catch {
    return null;
  }
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { auth = true, baseUrl = API_CONFIG.baseUrl, headers, ...rest } = options;

  const finalHeaders: Record<string, string> = {
    "Content-Type": "application/json",
    ...(headers as Record<string, string>),
  };

  if (auth) {
    const token = getAccessToken();
    if (token) finalHeaders.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${baseUrl}${path}`, {
    ...rest,
    headers: finalHeaders,
  });

  if (!response.ok) {
    let error: ApiError;
    try {
      const body = await response.json();
      error = { ...body, status: response.status };
    } catch {
      error = { code: "UNKNOWN", message: response.statusText, status: response.status };
    }
    throw new ApiRequestError(error);
  }

  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export const apiClient = {
  get: <T>(path: string, options?: RequestOptions) => request<T>(path, { ...options, method: "GET" }),
  post: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, method: "POST", body: body ? JSON.stringify(body) : undefined }),
  patch: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, method: "PATCH", body: body ? JSON.stringify(body) : undefined }),
  put: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, method: "PUT", body: body ? JSON.stringify(body) : undefined }),
  delete: <T>(path: string, options?: RequestOptions) => request<T>(path, { ...options, method: "DELETE" }),
};
