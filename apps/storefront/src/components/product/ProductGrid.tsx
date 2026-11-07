import type { ProductSummary } from "@zbob/shared-types";
import { Reveal } from "@/components/ui/Reveal";
import { ProductCard } from "./ProductCard";

export function ProductGrid({ products }: { products: ProductSummary[] }) {
  if (products.length === 0) {
    return <p className="py-16 text-center text-[16px] text-muted">New pieces are on the way. Please check back soon.</p>;
  }
  return (
    <div className="grid grid-cols-1 gap-x-[15px] gap-y-8 tablet:grid-cols-2 desktop:grid-cols-3">
      {products.map((product, index) => (
        <Reveal key={product.id} delay={(index % 3) * 0.06}>
          <ProductCard product={product} />
        </Reveal>
      ))}
    </div>
  );
}
