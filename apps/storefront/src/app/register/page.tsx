import type { Metadata } from "next";
import { Suspense } from "react";
import { RegisterForm } from "@/components/account/RegisterForm";

export const metadata: Metadata = { title: "Create account", robots: { index: false, follow: false } };

export default function RegisterPage() {
  return (
    <section className="mx-auto max-w-[460px] px-5 pt-[130px] pb-20">
      <Suspense>
        <RegisterForm />
      </Suspense>
    </section>
  );
}
