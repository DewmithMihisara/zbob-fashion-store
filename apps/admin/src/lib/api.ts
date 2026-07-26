import type { ApiError } from "@zbob/shared-types";
import { clearSession, getSession } from "./session";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

export class ApiRequestError extends Error {
  readonly status: number;
  readonly fieldErrors: Record<string, string>;

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  json?: unknown;
  form?: FormData;
}

export async function api<T = void>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = {};
  const token = getSession()?.token;
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  if (options.json !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      method: options.method ?? "GET",
      headers,
      body: options.form ?? (options.json === undefined ? undefined : JSON.stringify(options.json)),
    });
  } catch {
    throw new ApiRequestError(0, "Could not reach the server. Check your connection and try again.");
  }

  if (response.status === 401 && token) {
    clearSession(); // the session expired or was disabled; the layout sends the user to login
  }
  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as ApiError | null;
    throw new ApiRequestError(response.status, body?.message ?? "Something went wrong.", body?.fieldErrors);
  }
  // 202 and 204 answers, such as "reset link sent", have no body to read.
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

/** Reads the file name from a Content-Disposition header, falling back when there is none. */
export function fileNameFrom(header: string | null, fallback: string): string {
  const match = header?.match(/filename="?([^";]+)"?/i);
  return match?.[1] ?? fallback;
}

/** Downloads a file that needs the login token, which a plain link can not send. */
export async function downloadFile(path: string, fallbackName: string): Promise<void> {
  const token = getSession()?.token;
  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, { headers: token ? { Authorization: `Bearer ${token}` } : {} });
  } catch {
    throw new ApiRequestError(0, "Could not reach the server. Check your connection and try again.");
  }
  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as ApiError | null;
    throw new ApiRequestError(response.status, body?.message ?? "The download failed.");
  }
  const url = URL.createObjectURL(await response.blob());
  const link = document.createElement("a");
  link.href = url;
  link.download = fileNameFrom(response.headers.get("Content-Disposition"), fallbackName);
  document.body.append(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

export function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Something went wrong.";
}
