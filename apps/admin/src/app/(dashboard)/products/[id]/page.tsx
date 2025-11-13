"use client";

import type { AdminProduct } from "@zbob/shared-types";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { use, useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { PageHeader } from "@/components/ui/PageHeader";
import { ProductForm } from "@/features/products/ProductForm";
import { ProductImages } from "@/features/products/ProductImages";
import { ProductVariants } from "@/features/products/ProductVariants";
import { useData } from "@/hooks/useData";
import { useSession } from "@/hooks/useSession";
import { api, errorMessage } from "@/lib/api";

export default function EditProductPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const session = useSession();
  const { data: product, error, reload } = useData<AdminProduct>(`/admin/products/${id}`);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  async function remove() {
    try {
      await api(`/admin/products/${id}`, { method: "DELETE" });
      router.push("/products");
    } catch (e) {
      setDeleteError(errorMessage(e));
    }
  }

  return (
    <div className="max-w-[900px]">
      <Link href="/products" className="text-[13px] text-muted hover:underline">
        Back to products
      </Link>
      <PageHeader
        title={product?.name ?? "Product"}
        action={
          session?.role === "ADMIN" && product ? (
            <ConfirmButton onConfirm={remove}>Delete product</ConfirmButton>
          ) : undefined
        }
      />
      <ErrorAlert message={error ?? deleteError} />
      {product && (
        <div className="flex flex-col gap-5">
          <ProductImages productId={product.id} images={product.images} onChange={reload} />
          <ProductVariants productId={product.id} variants={product.variants} onChange={reload} />
          <ProductForm key={product.id} product={product} onSaved={reload} />
        </div>
      )}
    </div>
  );
}
