import type { Metadata } from "next";
import { ForgotPasswordForm } from "@/components/account/ForgotPasswordForm";

export const metadata: Metadata = { title: "Forgot password", robots: { index: false, follow: false } };

export default function ForgotPasswordPage() {
  return (
    <section className="mx-auto max-w-[460px] px-5 pt-[130px] pb-20">
      <ForgotPasswordForm />
    </section>
  );
}
