import type { CategoryWithPrices, SiteMedia } from "@zbob/shared-types";
import { formatPrice } from "@zbob/shared-utils";
import { LinkButton } from "@/components/ui/Button";
import { Container, Heading } from "@/components/ui/SectionHeading";
import { Reveal } from "@/components/ui/Reveal";
import { CollectionPhotos } from "./CollectionPhotos";

const SLOT_GROUPS = ["men", "women", "children"];
const HEADINGS = ["Sharp, easy pieces for men", "Soft, easy shapes for women", "Hard-wearing styles for children"];

export function CollectionsSection({ categories, media }: { categories: CategoryWithPrices[]; media: SiteMedia }) {
  return (
    <section className="pt-[90px]">
      <Container>
        <div className="flex flex-col gap-5 tablet:flex-row tablet:items-end tablet:justify-between">
          <Heading>
            Collections built on
            <br />
            simple, lasting ideas
          </Heading>
          <LinkButton href="/shop" className="self-start tablet:self-auto">
            Shop all items
          </LinkButton>
        </div>
        <div className="mt-[55px] flex flex-col gap-4">
          {categories.slice(0, 3).map((category, index) => {
            const group = SLOT_GROUPS[index];
            const photos = [1, 2, 3].map((n) => media[`collection-${group}-${n}`]);
            return (
              <Reveal key={category.id} className="grid gap-4 tablet:grid-cols-2">
                <div className={index % 2 === 1 ? "tablet:order-2" : ""}>
                  <CollectionPhotos photos={photos} alt={category.name} />
                </div>
                <div className="flex flex-col justify-center rounded-card bg-tile p-7">
                  <span className="inline-flex items-center gap-[8px] self-start rounded-full bg-paper p-[3px] pr-[13px] text-[14px]">
                    {category.badge && <span className="rounded-full bg-ink px-[10px] py-[3px] text-[13px] font-medium text-paper">{category.badge}</span>}
                    {category.name}
                  </span>
                  <h3 className="mt-6 max-w-[400px] text-[32px] leading-[1.15] font-medium tracking-tight-3 tablet:text-[44px]">{HEADINGS[index]}</h3>
                  <p className="mt-4 max-w-[440px] text-[16px] leading-[25px] font-medium text-muted">{category.description}</p>
                  <div className="mt-7 flex items-center justify-between gap-3 rounded-[10px] bg-paper p-2 pl-[18px]">
                    <div>
                      <p className="text-[14px] text-muted">Pricing starts from:</p>
                      {category.minPriceCents !== null && category.maxPriceCents !== null ? (
                        <p className="text-[17px]">
                          {formatPrice(category.minPriceCents)} <span className="px-1 text-faint">-</span> {formatPrice(category.maxPriceCents)}
                        </p>
                      ) : (
                        <p className="text-[17px]">Coming soon</p>
                      )}
                    </div>
                    <LinkButton href={`/shop?category=${category.slug}`} className="shrink-0">
                      All collections
                    </LinkButton>
                  </div>
                </div>
              </Reveal>
            );
          })}
        </div>
      </Container>
    </section>
  );
}
