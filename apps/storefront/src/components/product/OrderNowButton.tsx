/** Opens checkout for the chosen size. Payment itself is not connected yet; see the checkout page. */
export function OrderNowButton({ disabled = false, onOrder }: { disabled?: boolean; onOrder?: () => void }) {
  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onOrder}
      className="h-[54px] w-full rounded-full bg-ink text-[18px] font-medium tracking-tight-2 text-paper transition-transform enabled:hover:scale-[1.01] enabled:active:scale-[0.99] disabled:bg-faint"
    >
      {disabled ? "Out of stock" : "Order Now"}
    </button>
  );
}
