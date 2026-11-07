import type { Metadata } from "next";
import { Suspense } from "react";
import { ResetPasswordForm } from "@/components/account/ResetPasswordForm";

export const metadata: Metadata = { title: "Choose a new password", robots: { index: false, follow: false } };

export default function ResetPasswordPage() {
  return (
    <section className="mx-auto max-w-[460px] px-5 pt-[130px] pb-20">
      <Suspense>
        <ResetPasswordForm />
      </Suspense>
    </section>
  );
}
