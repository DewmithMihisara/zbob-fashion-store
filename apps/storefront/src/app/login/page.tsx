import type { Metadata } from "next";
import { Suspense } from "react";
import { LoginForm } from "@/components/account/LoginForm";

export const metadata: Metadata = { title: "Sign in", robots: { index: false, follow: false } };

export default function LoginPage() {
  return (
    <section className="mx-auto max-w-[460px] px-5 pt-[130px] pb-20">
      <Suspense>
        <LoginForm />
      </Suspense>
    </section>
  );
}
