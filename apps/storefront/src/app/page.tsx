import { BlogSection } from "@/components/home/BlogSection";
import { CollectionsSection } from "@/components/home/CollectionsSection";
import { CommunitySection } from "@/components/home/CommunitySection";
import { FeaturesSection } from "@/components/home/FeaturesSection";
import { HomeHero } from "@/components/home/HomeHero";
import { ProductSection } from "@/components/home/ProductSection";
import { ReviewsSection } from "@/components/home/ReviewsSection";
import { StorySection } from "@/components/home/StorySection";
import { CrownIcon, SparkleIcon } from "@/components/ui/icons";
import { JsonLd } from "@/components/seo/JsonLd";
import { getBlogPosts, getCategories, getProducts, getReviews, getSiteMedia } from "@/lib/api";
import { organizationJsonLd } from "@/lib/structured-data";

export const dynamic = "force-dynamic";

export default async function HomePage() {
  const [latest, bestSellers, categories, reviews, posts, media] = await Promise.all([
    getProducts({ badge: "NEW", size: 9 }),
    getProducts({ badge: "BEST_SELLER", size: 6 }),
    getCategories(),
    getReviews(),
    getBlogPosts({ size: 3 }),
    getSiteMedia(),
  ]);
  const heroPhotos = [1, 2, 3, 4, 5, 6, 7].map((n) => media[`home-hero-${n}`] ?? null);

  return (
    <>
      <JsonLd data={organizationJsonLd()} />
      <HomeHero photos={heroPhotos} />
      <ProductSection
        icon={<SparkleIcon size={14} />}
        label="New arrivals"
        title={
          <>
            Just landed in
            <br />
            our latest drop
          </>
        }
        products={latest.content}
      />
      <StorySection photo={media["home-story"]} />
      <ProductSection
        icon={<CrownIcon size={14} />}
        label="Best sellers"
        title={
          <>
            The pieces people
            <br />
            keep coming back for
          </>
        }
        products={bestSellers.content}
      />
      <CollectionsSection categories={categories} media={media} />
      <ReviewsSection summary={reviews} />
      <FeaturesSection media={media} />
      <BlogSection posts={posts.content} />
      <CommunitySection media={media} />
    </>
  );
}
