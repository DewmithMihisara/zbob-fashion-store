import type { InputHTMLAttributes, ReactNode, SelectHTMLAttributes, TextareaHTMLAttributes } from "react";

const controlClass =
  "mt-1 w-full rounded-lg border border-line bg-paper px-3 py-2 text-[14px] outline-none focus:border-ink disabled:bg-canvas";

function Wrapper({ label, error, hint, children }: { label: string; error?: string; hint?: string; children: ReactNode }) {
  return (
    <label className="block text-[13px] font-medium">
      {label}
      {children}
      {hint && !error && <span className="mt-1 block text-[12px] font-normal text-muted">{hint}</span>}
      {error && <span className="mt-1 block text-[12px] font-normal text-danger">{error}</span>}
    </label>
  );
}

type Common = { label: string; error?: string; hint?: string };

export function TextField({ label, error, hint, ...props }: Common & InputHTMLAttributes<HTMLInputElement>) {
  return (
    <Wrapper label={label} error={error} hint={hint}>
      <input className={controlClass} aria-invalid={Boolean(error)} {...props} />
    </Wrapper>
  );
}

export function TextArea({ label, error, hint, ...props }: Common & TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <Wrapper label={label} error={error} hint={hint}>
      <textarea className={controlClass} aria-invalid={Boolean(error)} {...props} />
    </Wrapper>
  );
}

export function SelectField({ label, error, hint, children, ...props }: Common & SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <Wrapper label={label} error={error} hint={hint}>
      <select className={controlClass} aria-invalid={Boolean(error)} {...props}>
        {children}
      </select>
    </Wrapper>
  );
}

export function CheckboxField({ label, ...props }: { label: string } & InputHTMLAttributes<HTMLInputElement>) {
  return (
    <label className="flex items-center gap-2 text-[13px] font-medium">
      <input type="checkbox" className="size-4 accent-black" {...props} />
      {label}
    </label>
  );
}
