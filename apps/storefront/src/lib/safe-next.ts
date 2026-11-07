/** Where to go after signing in. Only paths on this site are followed, so a crafted link can not send someone elsewhere. */
export function safeNext(value: string | null | undefined, fallback = "/account"): string {
  if (!value || !value.startsWith("/") || value.startsWith("//") || value.includes("\\")) {
    return fallback;
  }
  return value;
}
