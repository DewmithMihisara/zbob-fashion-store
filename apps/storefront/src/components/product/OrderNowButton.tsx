/**
 * Deliberately does nothing: zbob has no payment gateway yet.
 * Connect checkout here once one is chosen.
 */
export function OrderNowButton({ disabled = false }: { disabled?: boolean }) {
  return (
    <button
      type="button"
      disabled={disabled}
      className="h-[54px] w-full rounded-full bg-ink text-[18px] font-medium tracking-tight-2 text-paper transition-transform enabled:hover:scale-[1.01] enabled:active:scale-[0.99] disabled:bg-faint"
    >
      {disabled ? "Out of stock" : "Order Now"}
    </button>
  );
}
