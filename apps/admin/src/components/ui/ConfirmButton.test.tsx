import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ConfirmButton } from "./ConfirmButton";

afterEach(cleanup);

describe("ConfirmButton", () => {
  it("does not act on the first click", () => {
    const onConfirm = vi.fn();
    render(<ConfirmButton onConfirm={onConfirm}>Delete</ConfirmButton>);

    fireEvent.click(screen.getByRole("button", { name: "Delete" }));

    expect(onConfirm).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Confirm delete" })).toBeTruthy();
  });

  it("acts after confirmation", () => {
    const onConfirm = vi.fn();
    render(<ConfirmButton onConfirm={onConfirm}>Delete</ConfirmButton>);

    fireEvent.click(screen.getByRole("button", { name: "Delete" }));
    fireEvent.click(screen.getByRole("button", { name: "Confirm delete" }));

    expect(onConfirm).toHaveBeenCalledTimes(1);
  });

  it("can be cancelled", () => {
    const onConfirm = vi.fn();
    render(<ConfirmButton onConfirm={onConfirm}>Delete</ConfirmButton>);

    fireEvent.click(screen.getByRole("button", { name: "Delete" }));
    fireEvent.click(screen.getByRole("button", { name: "Cancel" }));

    expect(onConfirm).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "Delete" })).toBeTruthy();
  });
});
