import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { OrderNowButton } from "./OrderNowButton";

afterEach(cleanup);

describe("OrderNowButton", () => {
  it("is a plain button that is not inside a link or form", () => {
    render(<OrderNowButton />);
    const button = screen.getByRole("button", { name: "Order Now" });
    expect(button.getAttribute("type")).toBe("button");
    expect(button.closest("a")).toBeNull();
    expect(button.closest("form")).toBeNull();
  });

  it("makes no request and does not navigate when clicked", () => {
    const fetchSpy = vi.spyOn(globalThis, "fetch");
    const startUrl = window.location.href;
    render(<OrderNowButton />);

    fireEvent.click(screen.getByRole("button", { name: "Order Now" }));

    expect(fetchSpy).not.toHaveBeenCalled();
    expect(window.location.href).toBe(startUrl);
    fetchSpy.mockRestore();
  });

  it("shows out of stock and is disabled when nothing is available", () => {
    render(<OrderNowButton disabled />);
    const button = screen.getByRole("button", { name: "Out of stock" }) as HTMLButtonElement;
    expect(button.disabled).toBe(true);
  });
});
