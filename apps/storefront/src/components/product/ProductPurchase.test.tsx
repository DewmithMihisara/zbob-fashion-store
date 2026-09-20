import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { clearCustomerSession, setCustomerSession } from "@/lib/customer-session";
import { ProductPurchase } from "./ProductPurchase";

const push = vi.fn();
const addItem = vi.fn();

vi.mock("next/navigation", () => ({ useRouter: () => ({ push }) }));
vi.mock("@/components/cart/CartProvider", () => ({ useCart: () => ({ addItem }) }));

const options = [
  { variantId: 1, size: "S", color: "Black", inStock: false, colorHex: null },
  { variantId: 2, size: "M", color: "Black", inStock: true, colorHex: null },
  { variantId: 3, size: "L", color: "Black", inStock: true, colorHex: null },
];

beforeEach(() => {
  clearCustomerSession();
  push.mockReset();
  addItem.mockReset();
});

afterEach(cleanup);

describe("ProductPurchase", () => {
  it("starts on the first size in stock and disables sold out sizes", () => {
    render(<ProductPurchase slug="tee" options={options} />);

    expect(screen.getByRole("button", { name: "M" }).getAttribute("aria-pressed")).toBe("true");
    expect((screen.getByRole("button", { name: "S" }) as HTMLButtonElement).disabled).toBe(true);
  });

  it("sends signed out shoppers to sign in and back, without adding", () => {
    render(<ProductPurchase slug="tee" options={options} />);

    fireEvent.click(screen.getByRole("button", { name: "Add to cart" }));

    expect(push).toHaveBeenCalledWith("/login?next=%2Fshop%2Ftee");
    expect(addItem).not.toHaveBeenCalled();
  });

  it("adds the chosen size when signed in", async () => {
    setCustomerSession({ token: "abc", fullName: "Sam" });
    addItem.mockResolvedValue(undefined);
    render(<ProductPurchase slug="tee" options={options} />);

    fireEvent.click(screen.getByRole("button", { name: "L" }));
    fireEvent.click(screen.getByRole("button", { name: "Add to cart" }));

    await waitFor(() => expect(screen.getByRole("status").textContent).toContain("Added to your cart"));
    expect(addItem).toHaveBeenCalledWith(3, 1);
  });

  it("shows why adding failed", async () => {
    setCustomerSession({ token: "abc", fullName: "Sam" });
    addItem.mockRejectedValue(new Error("Only 1 left in stock"));
    render(<ProductPurchase slug="tee" options={options} />);

    fireEvent.click(screen.getByRole("button", { name: "Add to cart" }));

    await waitFor(() => expect(screen.getByRole("alert").textContent).toBe("Only 1 left in stock"));
  });
});
