import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, beforeAll, describe, expect, it, vi } from "vitest";
import { ColumnChart } from "./ColumnChart";

beforeAll(() => {
  // jsdom has no layout, so give the chart a width as a browser would.
  vi.stubGlobal(
    "ResizeObserver",
    class {
      constructor(private callback: ResizeObserverCallback) {}
      observe() {
        this.callback([{ contentRect: { width: 600 } } as ResizeObserverEntry], this as unknown as ResizeObserver);
      }
      disconnect() {}
    },
  );
});

afterEach(cleanup);

const columns = [
  { key: "a", label: "Nov 1", value: 1200 },
  { key: "b", label: "Nov 2", value: 0 },
  { key: "c", label: "Nov 3", value: 5300 },
];

describe("ColumnChart", () => {
  it("describes each day for screen readers", () => {
    render(<ColumnChart label="Revenue" columns={columns} format={(v) => `$${v}`} tickFormat={String} />);
    expect(screen.getByRole("img", { name: "Revenue" })).toBeTruthy();
    expect(screen.getByLabelText("Nov 3: $5300")).toBeTruthy();
  });

  it("shows a tooltip with the value on hover and hides it on leave", () => {
    render(<ColumnChart label="Revenue" columns={columns} format={(v) => `$${v}`} tickFormat={String} />);
    const target = screen.getByLabelText("Nov 1: $1200");

    fireEvent.mouseEnter(target);
    expect(screen.getByRole("tooltip").textContent).toContain("$1200");

    fireEvent.mouseLeave(target);
    expect(screen.queryByRole("tooltip")).toBeNull();
  });

  it("draws round tick values on the axis", () => {
    const { container } = render(<ColumnChart label="Revenue" columns={columns} format={String} tickFormat={String} />);
    const ticks = [...container.querySelectorAll("text")].map((t) => t.textContent).filter((t) => /^\d+$/.test(t ?? ""));
    expect(ticks).toEqual(["0", "2000", "4000", "6000"]);
  });
});
