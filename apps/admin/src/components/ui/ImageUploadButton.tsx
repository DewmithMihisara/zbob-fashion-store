"use client";

import { useRef, type ReactNode } from "react";
import { imageFileProblem } from "@/lib/image-files";
import { Button } from "./Button";

export function ImageUploadButton({
  onFiles,
  onProblem,
  multiple = false,
  disabled,
  variant = "secondary",
  children,
}: {
  onFiles: (files: File[]) => void;
  onProblem: (problem: string) => void;
  multiple?: boolean;
  disabled?: boolean;
  variant?: "primary" | "secondary";
  children: ReactNode;
}) {
  const input = useRef<HTMLInputElement>(null);

  return (
    <>
      <input
        ref={input}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        multiple={multiple}
        hidden
        aria-label="Choose image file"
        onChange={(event) => {
          const files = Array.from(event.target.files ?? []);
          event.target.value = ""; // so choosing the same file again still fires a change
          const problem = files.map(imageFileProblem).find(Boolean);
          if (problem) {
            onProblem(problem);
          } else if (files.length > 0) {
            onFiles(files);
          }
        }}
      />
      <Button variant={variant} disabled={disabled} onClick={() => input.current?.click()}>
        {children}
      </Button>
    </>
  );
}
