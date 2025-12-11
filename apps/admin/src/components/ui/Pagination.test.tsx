import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { Pagination } from "./Pagination";

afterEach(cleanup);

const page = (number: number, totalPages: number) => ({ content: [], page: number, size: 20, totalElements: 25, totalPages });

describe("Pagination", () => {
  it("hides when everything fits on one page", () => {
    const { container } = render(<Pagination page={page(0, 1)} onChange={vi.fn()} />);
    expect(container.innerHTML).toBe("");
  });

  it("moves between pages", () => {
    const onChange = vi.fn();
    render(<Pagination page={page(1, 3)} onChange={onChange} />);

    fireEvent.click(screen.getByRole("button", { name: "Next" }));
    fireEvent.click(screen.getByRole("button", { name: "Previous" }));

    expect(onChange.mock.calls).toEqual([[2], [0]]);
  });

  it("disables the ends", () => {
    render(<Pagination page={page(0, 2)} onChange={vi.fn()} />);
    expect((screen.getByRole("button", { name: "Previous" }) as HTMLButtonElement).disabled).toBe(true);
  });

  it("offers a way back when the last row of a later page was deleted", () => {
    const onChange = vi.fn();
    render(<Pagination page={page(1, 1)} onChange={onChange} />);

    fireEvent.click(screen.getByRole("button", { name: "Previous" }));

    expect(onChange).toHaveBeenCalledWith(0);
  });
});
