"use client";

import type { AuthResponse } from "@zbob/shared-types";
import { useRouter } from "next/navigation";
import { useEffect, useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { TextField } from "@/components/ui/Field";
import { useSession } from "@/hooks/useSession";
import { api, errorMessage } from "@/lib/api";
import { setSession } from "@/lib/session";

export default function LoginPage() {
  const router = useRouter();
  const session = useSession();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (session) {
      router.replace("/dashboard");
    }
  }, [session, router]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const auth = await api<AuthResponse>("/auth/admin/login", { method: "POST", json: { email, password } });
      setSession({ token: auth.accessToken, role: auth.role, fullName: auth.fullName });
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center px-4">
      <form onSubmit={submit} className="flex w-full max-w-[360px] flex-col gap-4 rounded-2xl bg-paper p-8 shadow-sm">
        <h1 className="text-[22px] font-semibold tracking-tight">zbob admin</h1>
        <ErrorAlert message={error} />
        <TextField label="Email" type="email" required autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} />
        <TextField label="Password" type="password" required autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <Button type="submit" disabled={busy} className="mt-2 h-10">
          {busy ? "Signing in..." : "Sign in"}
        </Button>
      </form>
    </main>
  );
}
