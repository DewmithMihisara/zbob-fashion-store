import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { StockEditor } from "./StockEditor";

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

function setup(quantity = 5) {
  const onSaved = vi.fn();
  const onError = vi.fn();
  render(<StockEditor variantId={7} quantity={quantity} label="Tee M" onSaved={onSaved} onError={onError} />);
  return { onSaved, onError, input: screen.getByLabelText("Stock for Tee M") as HTMLInputElement };
}

describe("StockEditor", () => {
  it("hides save until the number changes", () => {
    const { input } = setup();
    expect(screen.queryByRole("button", { name: "Save" })).toBeNull();
    fireEvent.change(input, { target: { value: "9" } });
    expect(screen.getByRole("button", { name: "Save" })).toBeTruthy();
  });

  it("saves the new quantity to the variant", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response("{}", { status: 200 }));
    vi.stubGlobal("fetch", fetchMock);
    const { input, onSaved } = setup();

    fireEvent.change(input, { target: { value: "0" } });
    fireEvent.click(screen.getByRole("button", { name: "Save" }));

    await waitFor(() => expect(onSaved).toHaveBeenCalled());
    expect(fetchMock.mock.calls[0]![0]).toContain("/admin/variants/7/stock");
    expect(fetchMock.mock.calls[0]![1].body).toBe('{"stockQuantity":0}');
  });

  it("does not allow negative or fractional stock", () => {
    const { input } = setup();
    for (const bad of ["-1", "1.5"]) {
      fireEvent.change(input, { target: { value: bad } });
      expect((screen.getByRole("button", { name: "Save" }) as HTMLButtonElement).disabled).toBe(true);
    }
  });

  it("reports a failed save", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response('{"message":"Nope"}', { status: 400 })));
    const { input, onError } = setup();

    fireEvent.change(input, { target: { value: "3" } });
    fireEvent.click(screen.getByRole("button", { name: "Save" }));

    await waitFor(() => expect(onError).toHaveBeenCalledWith("Nope"));
  });
});
