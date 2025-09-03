import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { CommunitySection } from "@/components/home/CommunitySection";
import { OrderNowButton } from "@/components/product/OrderNowButton";
import { Price } from "@/components/product/Price";
import { ProductGallery } from "@/components/product/ProductGallery";
import { TrustGrid } from "@/components/product/TrustGrid";
import { CheckIcon, CubeIcon, WandIcon } from "@/components/ui/icons";
import { Container } from "@/components/ui/SectionHeading";
import { getProduct, getSiteMedia } from "@/lib/api";

export const dynamic = "force-dynamic";

type Props = { params: Promise<{ slug: string }> };

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const product = await getProduct((await params).slug);
  return product ? { title: product.name, description: product.description } : { title: "Not found" };
}

export default async function ProductPage({ params }: Props) {
  const [product, media] = await Promise.all([getProduct((await params).slug), getSiteMedia()]);
  if (!product) {
    notFound();
  }

  const details = [
    { icon: CubeIcon, label: "Material", value: product.material },
    { icon: WandIcon, label: "Care", value: product.care },
    { icon: CheckIcon, label: "Warranty", value: product.warranty },
  ].filter((detail) => detail.value);

  return (
    <>
      <section className="pt-[98px]">
        <Container>
          <div className="grid gap-[34px] tablet:grid-cols-[575fr_541fr]">
            <ProductGallery images={product.images} name={product.name} badge={product.badge} />
            <div className="flex flex-col pr-0 desktop:pr-[34px]">
              <p className="mt-[34px] flex items-center gap-3 text-[17px] font-medium">
                <Link href="/shop" className="rounded-full bg-paper px-[14px] py-[7px]">
                  Shop
                </Link>
                <span aria-hidden="true" className="size-[6px] rounded-full bg-ink" />
                <Link href={`/shop?category=${product.category.slug}`} className="text-muted">
                  {product.category.name}
                </Link>
              </p>
              <h1 className="mt-3 text-[34px] leading-[1.15] font-medium tracking-tight-3 tablet:text-[44px]">{product.name}</h1>
              <div className="mt-3">
                <Price cents={product.priceCents} wasCents={product.compareAtPriceCents} prefix="USD " className="text-[24px]" wasClassName="text-[20px]" />
              </div>
              <p className="mt-10 max-w-[440px] text-[18px] leading-[27px] font-medium text-muted">{product.description}</p>
              <div className="mt-5">
                <OrderNowButton disabled={!product.inStock} />
              </div>
              <dl className="mt-auto pt-[58px]">
                {details.map(({ icon: DetailIcon, label, value }, index) => (
                  <div key={label} className={`grid grid-cols-[160px_1fr] py-[14px] ${index < details.length - 1 ? "border-b border-line" : ""}`}>
                    <dt className="flex items-center gap-[10px] text-[16px] font-medium">
                      <DetailIcon size={17} />
                      {label}
                    </dt>
                    <dd className="text-[16px] font-medium text-muted">{value}</dd>
                  </div>
                ))}
              </dl>
            </div>
          </div>
          <TrustGrid />
        </Container>
      </section>
      <CommunitySection media={media} />
    </>
  );
}
