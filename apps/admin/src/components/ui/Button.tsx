import type { ButtonHTMLAttributes } from "react";

type Variant = "primary" | "secondary" | "danger";

const variants: Record<Variant, string> = {
  primary: "bg-ink text-paper hover:bg-neutral-800",
  secondary: "bg-paper text-ink border border-line hover:bg-canvas",
  danger: "bg-paper text-danger border border-line hover:bg-red-50",
};

export function Button({
  variant = "primary",
  className = "",
  type = "button",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant }) {
  return (
    <button
      type={type}
      className={`inline-flex h-9 items-center justify-center rounded-lg px-3 text-[13px] font-medium whitespace-nowrap transition-colors disabled:cursor-not-allowed disabled:opacity-50 ${variants[variant]} ${className}`}
      {...props}
    />
  );
}
