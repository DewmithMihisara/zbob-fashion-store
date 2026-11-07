"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { PageHeader } from "@/components/ui/PageHeader";
import { ProductForm } from "@/features/products/ProductForm";

export default function NewProductPage() {
  const router = useRouter();
  return (
    <div className="max-w-[760px]">
      <Link href="/products" className="text-[13px] text-muted hover:underline">
        Back to products
      </Link>
      <PageHeader title="Add product" description="After creating it, you can add photos and stock." />
      <ProductForm onSaved={(product) => router.push(`/products/${product.id}`)} />
    </div>
  );
}
