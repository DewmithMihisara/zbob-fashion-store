import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { clearCustomerSession, setCustomerSession } from "@/lib/customer-session";
import { WishlistButton } from "./WishlistButton";

const push = vi.fn();
const toggle = vi.fn();
let saved = false;

vi.mock("next/navigation", () => ({ useRouter: () => ({ push }), usePathname: () => "/shop/tee" }));
vi.mock("./WishlistProvider", () => ({ useWishlist: () => ({ has: () => saved, toggle }) }));

beforeEach(() => {
  clearCustomerSession();
  push.mockReset();
  toggle.mockReset();
  saved = false;
});

afterEach(cleanup);

describe("WishlistButton", () => {
  it("sends signed out shoppers to sign in and back", () => {
    render(<WishlistButton productId={1} productName="Tee" />);

    fireEvent.click(screen.getByRole("button", { name: "Save Tee to wishlist" }));

    expect(push).toHaveBeenCalledWith("/login?next=%2Fshop%2Ftee");
    expect(toggle).not.toHaveBeenCalled();
  });

  it("saves when signed in", async () => {
    setCustomerSession({ token: "abc", fullName: "Sam" });
    toggle.mockResolvedValue(undefined);
    render(<WishlistButton productId={7} productName="Tee" />);

    fireEvent.click(screen.getByRole("button", { name: "Save Tee to wishlist" }));

    await waitFor(() => expect(toggle).toHaveBeenCalledWith(7));
  });

  it("shows a saved product as pressed", () => {
    saved = true;
    render(<WishlistButton productId={1} productName="Tee" />);

    expect(screen.getByRole("button", { name: "Remove Tee from wishlist" }).getAttribute("aria-pressed")).toBe("true");
  });
});
