import { LinkButton } from "@/components/ui/Button";

export default function NotFound() {
  return (
    <section className="flex min-h-[70vh] flex-col items-center justify-center gap-4 px-5 pt-24 text-center">
      <h1 className="text-[40px] font-medium tracking-tight-3">Page not found</h1>
      <p className="max-w-[420px] text-[16px] text-muted">This page may have moved, or the item may no longer be available.</p>
      <LinkButton href="/shop">Shop all items</LinkButton>
    </section>
  );
}
