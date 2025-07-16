import Link from "next/link";

export function Wordmark({ className = "" }: { className?: string }) {
  return (
    <Link href="/" aria-label="zbob home" className={`font-logo text-[24px] leading-none tracking-[0.04em] uppercase ${className}`}>
      zbob
    </Link>
  );
}
