"use client";

import type { AdminProduct, Brand, Category } from "@zbob/shared-types";
import { centsToInput, parsePrice } from "@zbob/shared-utils";
import { useState, type FormEvent } from "react";
import { ErrorAlert, SavedAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { CheckboxField, SelectField, TextArea, TextField } from "@/components/ui/Field";
import { useData } from "@/hooks/useData";
import { api, ApiRequestError, errorMessage } from "@/lib/api";

export function ProductForm({ product, onSaved }: { product?: AdminProduct; onSaved: (saved: AdminProduct) => void }) {
  const { data: categories } = useData<Category[]>("/categories");
  const { data: brands } = useData<Brand[]>("/brands");

  const [name, setName] = useState(product?.name ?? "");
  const [description, setDescription] = useState(product?.description ?? "");
  const [material, setMaterial] = useState(product?.material ?? "");
  const [care, setCare] = useState(product?.care ?? "");
  const [warranty, setWarranty] = useState(product?.warranty ?? "");
  const [badge, setBadge] = useState<string>(product?.badge ?? "");
  const [price, setPrice] = useState(centsToInput(product?.priceCents));
  const [originalPrice, setOriginalPrice] = useState(centsToInput(product?.compareAtPriceCents));
  const [published, setPublished] = useState(product?.published ?? false);
  const [categoryId, setCategoryId] = useState(product ? String(product.category.id) : "");
  const [brandId, setBrandId] = useState(product?.brand ? String(product.brand.id) : "");

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSaved(null);
    setFieldErrors({});

    const priceCents = parsePrice(price);
    const originalCents = originalPrice.trim() ? parsePrice(originalPrice) : null;
    if (priceCents === null || (originalPrice.trim() && originalCents === null)) {
      setFieldErrors({
        ...(priceCents === null ? { priceCents: "Enter a price like 35.00" } : {}),
        ...(originalPrice.trim() && originalCents === null ? { compareAtPriceCents: "Enter a price like 45.00" } : {}),
      });
      return;
    }

    const body = {
      name,
      description,
      material: material || null,
      care: care || null,
      warranty: warranty || null,
      badge: badge || null,
      priceCents,
      compareAtPriceCents: originalCents,
      published,
      categoryId: categoryId ? Number(categoryId) : null,
      brandId: brandId ? Number(brandId) : null,
    };

    setBusy(true);
    try {
      const result = product
        ? await api<AdminProduct>(`/admin/products/${product.id}`, { method: "PUT", json: body })
        : await api<AdminProduct>("/admin/products", { method: "POST", json: body });
      setSaved("Saved.");
      onSaved(result);
    } catch (e) {
      setError(errorMessage(e));
      if (e instanceof ApiRequestError) {
        setFieldErrors(e.fieldErrors);
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-4 rounded-xl border border-line bg-paper p-5">
      <ErrorAlert message={error} />
      <SavedAlert message={saved} />
      <TextField label="Name" required maxLength={160} value={name} onChange={(e) => setName(e.target.value)} error={fieldErrors.name} />
      <TextArea label="Description" required rows={4} maxLength={4000} value={description} onChange={(e) => setDescription(e.target.value)} error={fieldErrors.description} />
      <div className="grid gap-4 sm:grid-cols-2">
        <TextField label="Price" required inputMode="decimal" placeholder="35.00" value={price} onChange={(e) => setPrice(e.target.value)} error={fieldErrors.priceCents} />
        <TextField label="Original price" inputMode="decimal" placeholder="45.00" hint="Shown crossed out. Leave empty if not on sale." value={originalPrice} onChange={(e) => setOriginalPrice(e.target.value)} error={fieldErrors.compareAtPriceCents} />
        <SelectField label="Category" required value={categoryId} onChange={(e) => setCategoryId(e.target.value)} error={fieldErrors.categoryId}>
          <option value="">Choose...</option>
          {categories?.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </SelectField>
        <SelectField label="Brand" value={brandId} onChange={(e) => setBrandId(e.target.value)}>
          <option value="">None</option>
          {brands?.map((b) => (
            <option key={b.id} value={b.id}>
              {b.name}
            </option>
          ))}
        </SelectField>
        <SelectField label="Badge" value={badge} onChange={(e) => setBadge(e.target.value)}>
          <option value="">None</option>
          <option value="NEW">New (shows in latest drop)</option>
          <option value="BEST_SELLER">Best seller</option>
        </SelectField>
      </div>
      <TextField label="Material" maxLength={500} value={material} onChange={(e) => setMaterial(e.target.value)} />
      <TextField label="Care" maxLength={500} value={care} onChange={(e) => setCare(e.target.value)} />
      <TextField label="Warranty" maxLength={500} value={warranty} onChange={(e) => setWarranty(e.target.value)} />
      <CheckboxField label="Published (visible on the website)" checked={published} onChange={(e) => setPublished(e.target.checked)} />
      <div>
        <Button type="submit" disabled={busy}>
          {busy ? "Saving..." : product ? "Save changes" : "Create product"}
        </Button>
      </div>
    </form>
  );
}
