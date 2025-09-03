import type { Metadata } from "next";
import { CommunitySection } from "@/components/home/CommunitySection";
import { LinkButton } from "@/components/ui/Button";
import { SectionEyebrow } from "@/components/ui/Eyebrow";
import { GlobeIcon } from "@/components/ui/icons";
import { PageHero } from "@/components/ui/PageHero";
import { Photo } from "@/components/ui/Photo";
import { Reveal } from "@/components/ui/Reveal";
import { Container } from "@/components/ui/SectionHeading";
import { getCategories, getProducts, getReviews, getSiteMedia } from "@/lib/api";

export const dynamic = "force-dynamic";

export const metadata: Metadata = {
  title: "About",
  description: "Why zbob exists and how we think about clothing.",
};

export default async function AboutPage() {
  const [products, categories, reviews, media] = await Promise.all([
    getProducts({ size: 1 }),
    getCategories(),
    getReviews(),
    getSiteMedia(),
  ]);

  // Live numbers from the store, so this block is always true.
  const figures = [
    { value: String(products.totalElements), label: "Styles available", photo: media["feature-1-front"] },
    { value: String(categories.length), label: "Collections", photo: media["feature-2-front"] },
    { value: String(reviews.reviewCount), label: "Customer reviews", photo: media["feature-3-front"] },
    {
      value: reviews.reviewCount > 0 ? `${reviews.averageRating}/5` : "New",
      label: reviews.reviewCount > 0 ? "Average rating" : "Be our first review",
      photo: media["feature-4-front"],
    },
  ];

  return (
    <>
      <PageHero
        lead="About"
        label="Get to know zbob"
        photo={media["about-hero"]}
        title={
          <>
            Simple design,
            <br />
            made for real life
          </>
        }
        text="We make essential clothing that stays useful, comfortable and good looking season after season."
      >
        <LinkButton href="/shop" variant="light">
          Browse collections
        </LinkButton>
        <LinkButton href="/contact" variant="glass">
          Contact us
        </LinkButton>
      </PageHero>
      <section className="pt-[110px]">
        <Container className="flex flex-col items-center text-center">
          <SectionEyebrow icon={<GlobeIcon size={14} />}>About zbob</SectionEyebrow>
          <p className="mt-5 max-w-[720px] text-[28px] leading-[1.25] font-medium tracking-tight-3 tablet:text-[36px]">
            We believe clothes should be easy to choose, easy to wear and easy to keep, so we design fewer pieces and take more care over each one.
          </p>
        </Container>
        <Container className="mt-[60px]">
          <div className="grid gap-[14px] tablet:grid-cols-2 desktop:grid-cols-4">
            {figures.map((figure) => (
              <Reveal key={figure.label} className="relative h-[300px] overflow-hidden rounded-card bg-ink text-paper">
                <Photo src={figure.photo} alt="" sizes="(min-width: 1200px) 25vw, 50vw" tone="dark" />
                <div className="absolute inset-0 bg-gradient-to-b from-transparent to-black/70" />
                <div className="absolute inset-x-0 bottom-5 text-center">
                  <p className="text-[48px] leading-none font-medium tracking-tight-3">{figure.value}</p>
                  <p className="mt-2 text-[18px] font-medium">{figure.label}</p>
                </div>
              </Reveal>
            ))}
          </div>
        </Container>
      </section>
      <CommunitySection media={media} />
    </>
  );
}
