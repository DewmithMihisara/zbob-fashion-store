import type { Metadata } from "next";
import { CommunitySection } from "@/components/home/CommunitySection";
import { ProductGrid } from "@/components/product/ProductGrid";
import { ShopPagination } from "@/components/shop/ShopPagination";
import { SortSelect } from "@/components/shop/SortSelect";
import { LinkButton } from "@/components/ui/Button";
import { FilterTabs } from "@/components/ui/FilterTabs";
import { SearchIcon } from "@/components/ui/icons";
import { PageHero } from "@/components/ui/PageHero";
import { Container } from "@/components/ui/SectionHeading";
import { getCategories, getProducts, getSiteMedia } from "@/lib/api";
import { parseShopQuery, shopHref } from "@/lib/shop-query";

export const dynamic = "force-dynamic";

export const metadata: Metadata = {
  title: "Shop",
  description: "Browse everyday clothing for men, women and children.",
  alternates: { canonical: "/shop" },
};

const PAGE_SIZE = 12;

export default async function ShopPage({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  const query = parseShopQuery(await searchParams);
  const [products, categories, media] = await Promise.all([
    getProducts({ category: query.category, search: query.search, order: query.order, page: query.page - 1, size: PAGE_SIZE }),
    getCategories(),
    getSiteMedia(),
  ]);

  const tabs = [
    { href: shopHref(query, { category: undefined, page: 1 }), label: "All Products", active: !query.category },
    ...categories.map((c) => ({ href: shopHref(query, { category: c.slug, page: 1 }), label: c.name, active: query.category === c.slug })),
  ];
  const total = products.totalElements;

  return (
    <>
      <PageHero
        lead="Shop"
        label="This season"
        photo={media["shop-hero"]}
        title={
          <>
            Everything you need,
            <br />
            nothing you don&apos;t
          </>
        }
        text="Browse pieces designed to mix, match and stay in your wardrobe for years, not months."
      >
        <LinkButton href="/blog" variant="light">
          Explore stories
        </LinkButton>
        <LinkButton href="/about" variant="glass">
          About us
        </LinkButton>
      </PageHero>
      <section className="pt-[86px]">
        <Container>
          <FilterTabs label="Filter products by category" tabs={tabs} />
          <div className="mt-6 flex flex-col gap-4 tablet:flex-row tablet:items-center tablet:justify-between">
            <form action="/shop" role="search" className="flex flex-1 items-center gap-2 tablet:max-w-[420px]">
              {query.category && <input type="hidden" name="category" value={query.category} />}
              {query.order !== "newest" && <input type="hidden" name="order" value={query.order} />}
              <label htmlFor="search" className="sr-only">
                Search products
              </label>
              <input
                id="search"
                name="search"
                type="search"
                defaultValue={query.search}
                maxLength={100}
                placeholder="Search products"
                className="h-11 min-w-0 flex-1 rounded-full bg-paper px-5 text-[16px] outline-none focus:outline-2 focus:outline-ink"
              />
              <button type="submit" aria-label="Search" className="flex size-11 shrink-0 items-center justify-center rounded-full bg-ink text-paper">
                <SearchIcon />
              </button>
            </form>
            <SortSelect query={query} />
          </div>
          <p className="mt-6 text-[15px] text-muted" aria-live="polite" data-testid="result-count">
            {total === 1 ? "1 product" : `${total} products`}
            {query.search && <> for &ldquo;{query.search}&rdquo;</>}
          </p>
          <div className="mt-4">
            <ProductGrid products={products.content} />
          </div>
          <ShopPagination query={query} totalPages={products.totalPages} />
        </Container>
      </section>
      <CommunitySection media={media} />
    </>
  );
}
