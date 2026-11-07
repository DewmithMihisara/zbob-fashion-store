"use client";

import type { SiteMediaSlot } from "@zbob/shared-types";
import { useMemo, useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { ImageUploadButton } from "@/components/ui/ImageUploadButton";
import { PageHeader } from "@/components/ui/PageHeader";
import { useData } from "@/hooks/useData";
import { api, errorMessage } from "@/lib/api";

function groupOf(slot: string): string {
  if (slot.startsWith("home-hero")) return "Home hero";
  if (slot.startsWith("collection-")) return "Collections";
  if (slot.startsWith("feature-")) return "Feature cards";
  if (slot.startsWith("community-")) return "Community gallery";
  return "Page photos";
}

const GROUP_ORDER = ["Home hero", "Page photos", "Collections", "Feature cards", "Community gallery"];

export default function SitePhotosPage() {
  const { data, error, reload } = useData<SiteMediaSlot[]>("/admin/site-media");
  const [actionError, setActionError] = useState<string | null>(null);
  const [busySlot, setBusySlot] = useState<string | null>(null);

  const groups = useMemo(
    () => GROUP_ORDER.map((title) => ({ title, slots: (data ?? []).filter((s) => groupOf(s.slot) === title) })),
    [data],
  );

  async function run(slot: string, action: () => Promise<unknown>) {
    setBusySlot(slot);
    setActionError(null);
    try {
      await action();
      reload();
    } catch (e) {
      setActionError(errorMessage(e));
    } finally {
      setBusySlot(null);
    }
  }

  function upload(slot: string, file: File) {
    const form = new FormData();
    form.append("file", file);
    return run(slot, () => api(`/admin/site-media/${slot}`, { method: "PUT", form }));
  }

  return (
    <>
      <PageHeader title="Site photos" description="Every photo on the website that is not a product. Empty slots show a placeholder on the website." />
      <ErrorAlert message={error ?? actionError} />
      {groups.map((group) => (
        <section key={group.title} className="mb-8">
          <h2 className="mb-3 text-[16px] font-semibold">{group.title}</h2>
          <ul className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {group.slots.map((slot) => (
              <li key={slot.slot} className="rounded-xl border border-line bg-paper p-3">
                <div className="aspect-[4/3] overflow-hidden rounded-lg bg-tile">
                  {slot.url ? (
                    <img src={slot.url} alt={slot.label} className="size-full object-cover" />
                  ) : (
                    <span className="flex size-full items-center justify-center text-[12px] text-faint">Placeholder showing</span>
                  )}
                </div>
                <p className="mt-2 text-[13px] font-medium">{slot.label}</p>
                <div className="mt-2 flex flex-wrap gap-1">
                  <ImageUploadButton disabled={busySlot === slot.slot} onFiles={([file]) => upload(slot.slot, file!)} onProblem={setActionError}>
                    {busySlot === slot.slot ? "Uploading..." : slot.url ? "Replace" : "Upload"}
                  </ImageUploadButton>
                  {slot.url && (
                    <ConfirmButton confirmLabel="Yes" onConfirm={() => run(slot.slot, () => api(`/admin/site-media/${slot.slot}`, { method: "DELETE" }))}>
                      Remove
                    </ConfirmButton>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </section>
      ))}
    </>
  );
}
