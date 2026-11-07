import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { OrderNowButton } from "./OrderNowButton";

afterEach(cleanup);

describe("OrderNowButton", () => {
  it("is a plain button that is not inside a link or form", () => {
    render(<OrderNowButton onOrder={vi.fn()} />);
    const button = screen.getByRole("button", { name: "Order Now" });
    expect(button.getAttribute("type")).toBe("button");
    expect(button.closest("a")).toBeNull();
    expect(button.closest("form")).toBeNull();
  });

  it("hands the order to its owner when clicked", () => {
    const onOrder = vi.fn();
    render(<OrderNowButton onOrder={onOrder} />);

    fireEvent.click(screen.getByRole("button", { name: "Order Now" }));

    expect(onOrder).toHaveBeenCalledTimes(1);
  });

  it("shows out of stock and is disabled when nothing is available", () => {
    render(<OrderNowButton disabled />);
    const button = screen.getByRole("button", { name: "Out of stock" }) as HTMLButtonElement;
    expect(button.disabled).toBe(true);
  });
});
