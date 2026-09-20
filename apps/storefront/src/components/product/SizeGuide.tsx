"use client";

import { useEffect, useRef, useState } from "react";
import { parseSizeGuide } from "@/lib/size-guide";

export function SizeGuide({ guide }: { guide: string }) {
  const [open, setOpen] = useState(false);
  const dialog = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    if (open && dialog.current && !dialog.current.open) {
      dialog.current.showModal();
    }
  }, [open]);

  return (
    <>
      <button type="button" onClick={() => setOpen(true)} className="text-[15px] text-muted underline hover:text-ink">
        Size guide
      </button>
      {open && (
        <dialog
          ref={dialog}
          onClose={() => setOpen(false)}
          aria-labelledby="size-guide-title"
          className="m-auto w-[min(640px,calc(100vw-32px))] rounded-card bg-paper p-6 backdrop:bg-black/40"
        >
          <div className="mb-4 flex items-center justify-between">
            <h2 id="size-guide-title" className="text-[24px] font-medium tracking-tight-3">
              Size guide
            </h2>
            <button type="button" aria-label="Close size guide" onClick={() => dialog.current?.close()} className="text-[24px] leading-none text-muted hover:text-ink">
              ×
            </button>
          </div>
          <div className="flex flex-col gap-3 text-[16px]">
            {parseSizeGuide(guide).map((block, index) =>
              block.type === "text" ? (
                <p key={index} className="text-muted">
                  {block.text}
                </p>
              ) : (
                <div key={index} className="overflow-x-auto">
                  <table className="w-full text-left">
                    <thead>
                      <tr className="border-b border-line">
                        {block.rows[0]!.map((cell, i) => (
                          <th key={i} scope="col" className="py-2 pr-4 font-medium">
                            {cell}
                          </th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {block.rows.slice(1).map((row, r) => (
                        <tr key={r} className="border-b border-line last:border-0">
                          {row.map((cell, i) => (
                            <td key={i} className="py-2 pr-4 text-muted">
                              {cell}
                            </td>
                          ))}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ),
            )}
          </div>
        </dialog>
      )}
    </>
  );
}
