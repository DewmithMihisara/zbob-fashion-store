"use client";

import { useEffect, useRef, type ReactNode } from "react";

/** Uses the native dialog, which traps focus and closes on Escape. */
export function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: ReactNode }) {
  const ref = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const dialog = ref.current;
    if (dialog && !dialog.open) {
      dialog.showModal();
    }
  }, []);

  return (
    <dialog
      ref={ref}
      onClose={onClose}
      aria-labelledby="modal-title"
      className="m-auto w-[min(560px,calc(100vw-32px))] rounded-2xl p-6 backdrop:bg-black/40"
    >
      <div className="mb-4 flex items-center justify-between">
        <h2 id="modal-title" className="text-[18px] font-semibold">
          {title}
        </h2>
        <button type="button" aria-label="Close" onClick={onClose} className="text-[20px] leading-none text-muted hover:text-ink">
          ×
        </button>
      </div>
      {children}
    </dialog>
  );
}
