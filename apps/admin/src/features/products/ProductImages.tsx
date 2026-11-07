"use client";

import type { ProductImage } from "@zbob/shared-types";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { ImageUploadButton } from "@/components/ui/ImageUploadButton";
import { api, errorMessage } from "@/lib/api";
import { MAX_PRODUCT_PHOTOS, photoLimitProblem } from "@/lib/product-photos";

/** Upload, order and remove a product's photos. The first photo is the one shown on product cards. */
export function ProductImages({ productId, images, onChange }: { productId: number; images: ProductImage[]; onChange: () => void }) {
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function run(action: () => Promise<unknown>) {
    setBusy(true);
    setError(null);
    try {
      await action();
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
      onChange();
    }
  }

  function upload(files: File[]) {
    const problem = photoLimitProblem(images.length, files.length);
    if (problem) {
      setError(problem);
      return;
    }
    return run(async () => {
      for (const file of files) {
        const form = new FormData();
        form.append("file", file);
        await api(`/admin/products/${productId}/images`, { method: "POST", form });
      }
    });
  }

  function move(index: number, offset: number) {
    const ids = images.map((image) => image.id);
    const [moved] = ids.splice(index, 1);
    ids.splice(index + offset, 0, moved!);
    return run(() => api(`/admin/products/${productId}/images/order`, { method: "PUT", json: { imageIds: ids } }));
  }

  return (
    <section className="rounded-xl border border-line bg-paper p-5">
      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <div>
          <h2 className="text-[16px] font-semibold">
            Photos ({images.length} of {MAX_PRODUCT_PHOTOS})
          </h2>
          <p className="text-[13px] text-muted">The first photo appears on product cards. The website shows a placeholder until you add one.</p>
        </div>
        <ImageUploadButton multiple variant="primary" disabled={busy || images.length >= MAX_PRODUCT_PHOTOS} onFiles={upload} onProblem={setError}>
          {busy ? "Working..." : images.length >= MAX_PRODUCT_PHOTOS ? "Limit reached" : "Upload photos"}
        </ImageUploadButton>
      </div>
      <ErrorAlert message={error} />
      {images.length === 0 ? (
        <p className="rounded-lg bg-canvas py-10 text-center text-[13px] text-muted">No photos yet.</p>
      ) : (
        <ul className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
          {images.map((image, index) => (
            <li key={image.id} className="rounded-lg border border-line p-2">
              <div className="relative aspect-square overflow-hidden rounded-md bg-tile">
                <img src={image.url} alt={image.altText ?? `Photo ${index + 1}`} className="size-full object-cover" />
                {index === 0 && <span className="absolute top-1 left-1 rounded bg-ink px-1 text-[11px] text-paper">Main</span>}
              </div>
              <div className="mt-2 flex flex-wrap gap-1">
                <Button variant="secondary" aria-label={`Move photo ${index + 1} earlier`} disabled={busy || index === 0} onClick={() => move(index, -1)}>
                  ←
                </Button>
                <Button variant="secondary" aria-label={`Move photo ${index + 1} later`} disabled={busy || index === images.length - 1} onClick={() => move(index, 1)}>
                  →
                </Button>
                <ConfirmButton disabled={busy} confirmLabel="Yes" onConfirm={() => run(() => api(`/admin/products/${productId}/images/${image.id}`, { method: "DELETE" }))}>
                  Delete
                </ConfirmButton>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
