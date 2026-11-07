import type { ApiError } from "@zbob/shared-types";
import { clearCustomerSession, getCustomerSession } from "./customer-session";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

export class CustomerApiError extends Error {
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
}

export async function customerApi<T = void>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = {};
  const token = getCustomerSession()?.token;
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
      body: options.json === undefined ? undefined : JSON.stringify(options.json),
    });
  } catch {
    throw new CustomerApiError(0, "We could not reach the store. Check your connection and try again.");
  }

  if (response.status === 401 && token) {
    clearCustomerSession(); // the login expired or the account was disabled
  }
  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as ApiError | null;
    throw new CustomerApiError(response.status, body?.message ?? "Something went wrong. Please try again.", body?.fieldErrors);
  }
  // 202 and 204 answers, such as "reset link sent", have no body to read.
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export function customerErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Something went wrong. Please try again.";
}
