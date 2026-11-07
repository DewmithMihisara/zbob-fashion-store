import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { BulkStockBar } from "./BulkStockBar";

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe("BulkStockBar", () => {
  it("sends the ticked rows with the change", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response('{"updated":2}', { status: 200 }));
    vi.stubGlobal("fetch", fetchMock);
    const onDone = vi.fn();
    render(<BulkStockBar variantIds={[3, 4]} onDone={onDone} onClear={vi.fn()} />);

    fireEvent.change(screen.getByLabelText("Amount"), { target: { value: "-2" } });
    fireEvent.click(screen.getByRole("button", { name: "Apply" }));

    await waitFor(() => expect(onDone).toHaveBeenCalled());
    expect(JSON.parse(fetchMock.mock.calls[0]![1].body)).toEqual({ variantIds: [3, 4], mode: "ADJUST", amount: -2 });
  });

  it("does not allow setting below zero or adding nothing", () => {
    render(<BulkStockBar variantIds={[1]} onDone={vi.fn()} onClear={vi.fn()} />);
    const apply = screen.getByRole("button", { name: "Apply" }) as HTMLButtonElement;

    fireEvent.change(screen.getByLabelText("Amount"), { target: { value: "0" } });
    expect(apply.disabled).toBe(true);

    fireEvent.change(screen.getByLabelText("Change type"), { target: { value: "SET" } });
    fireEvent.change(screen.getByLabelText("Amount"), { target: { value: "-1" } });
    expect(apply.disabled).toBe(true);
  });

  it("shows why a change was refused", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response('{"message":"This would take stock below 0 for: A-M"}', { status: 409 })));
    render(<BulkStockBar variantIds={[1]} onDone={vi.fn()} onClear={vi.fn()} />);

    fireEvent.change(screen.getByLabelText("Amount"), { target: { value: "-9" } });
    fireEvent.click(screen.getByRole("button", { name: "Apply" }));

    await waitFor(() => expect(screen.getByRole("alert").textContent).toContain("below 0 for: A-M"));
  });
});
