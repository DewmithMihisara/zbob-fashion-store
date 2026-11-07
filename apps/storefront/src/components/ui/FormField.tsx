import type { InputHTMLAttributes, ReactNode } from "react";

export const inputClass =
  "mt-2 w-full rounded-[10px] bg-canvas px-[14px] py-[13px] text-[17px] placeholder:text-faint focus:outline-2 focus:outline-ink disabled:cursor-not-allowed disabled:bg-transparent disabled:text-muted";

export function FormField({
  label,
  error,
  hint,
  ...props
}: { label: string; error?: string; hint?: ReactNode } & InputHTMLAttributes<HTMLInputElement>) {
  return (
    <label className="block text-[16px] font-medium">
      {label}
      <input className={inputClass} aria-invalid={Boolean(error)} {...props} />
      {hint && !error && <span className="mt-1 block text-[14px] text-muted">{hint}</span>}
      {error && <span className="mt-1 block text-[14px] text-red-700">{error}</span>}
    </label>
  );
}

export function ErrorNotice({ message }: { message: string | null | undefined }) {
  if (!message) {
    return null;
  }
  return (
    <p data-testid="form-error" className="rounded-[10px] bg-red-50 px-[14px] py-[10px] text-[15px] text-red-700" role="alert">
      {message}
    </p>
  );
}

export function SavedNotice({ message }: { message: string | null | undefined }) {
  if (!message) {
    return null;
  }
  return (
    <p className="rounded-[10px] bg-green-50 px-[14px] py-[10px] text-[15px] text-green-800" role="status">
      {message}
    </p>
  );
}
