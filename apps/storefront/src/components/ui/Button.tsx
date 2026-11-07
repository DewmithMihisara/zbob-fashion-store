import Link from "next/link";
import type { ComponentProps, ReactNode } from "react";

type Variant = "dark" | "light" | "glass" | "ghost";

const base =
  "inline-flex items-center justify-center rounded-full px-[15px] py-[10px] text-[15px] font-medium " +
  "leading-[19px] tracking-tight-2 transition-transform duration-200 hover:scale-[1.03] active:scale-[0.98]";

const variants: Record<Variant, string> = {
  dark: "bg-ink text-paper",
  light: "bg-paper text-ink",
  glass: "bg-paper/15 text-paper backdrop-blur-md",
  ghost: "bg-paper text-ink",
};

export function buttonClass(variant: Variant = "dark", className = ""): string {
  return `${base} ${variants[variant]} ${className}`;
}

export function LinkButton({
  variant,
  className,
  children,
  ...props
}: ComponentProps<typeof Link> & { variant?: Variant; children: ReactNode }) {
  return (
    <Link {...props} className={buttonClass(variant, className)}>
      {children}
    </Link>
  );
}
