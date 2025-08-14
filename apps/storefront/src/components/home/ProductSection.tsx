import type { ProductSummary } from "@zbob/shared-types";
import type { ReactNode } from "react";
import { ProductGrid } from "@/components/product/ProductGrid";
import { LinkButton } from "@/components/ui/Button";
import { SectionEyebrow } from "@/components/ui/Eyebrow";
import { Container, SplitHeading } from "@/components/ui/SectionHeading";

export function ProductSection({
  icon,
  label,
  title,
  products,
}: {
  icon: ReactNode;
  label: string;
  title: ReactNode;
  products: ProductSummary[];
}) {
  return (
    <section className="pt-[90px]">
      <Container>
        <SplitHeading
          eyebrow={<SectionEyebrow icon={icon}>{label}</SectionEyebrow>}
          title={title}
          action={<LinkButton href="/shop" className="self-start tablet:self-auto">See all collections</LinkButton>}
        />
        <div className="mt-[55px]">
          <ProductGrid products={products} />
        </div>
      </Container>
    </section>
  );
}
