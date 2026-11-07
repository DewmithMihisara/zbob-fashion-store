"use client";

import type { ProductReviews } from "@zbob/shared-types";
import { useEffect, useState } from "react";
import { buttonClass } from "@/components/ui/Button";
import { Container } from "@/components/ui/SectionHeading";
import { customerApi } from "@/lib/customer-api";
import { ReviewForm } from "./ReviewForm";
import { Stars } from "./Stars";

const PAGE_SIZE = 5;

export function ProductReviewsSection({ slug, productId }: { slug: string; productId: number }) {
  const [page, setPage] = useState(0);
  const [loaded, setLoaded] = useState<{ page: number; data: ProductReviews } | null>(null);

  useEffect(() => {
    let current = true;
    customerApi<ProductReviews>(`/products/${encodeURIComponent(slug)}/reviews?page=${page}&size=${PAGE_SIZE}`).then(
      (data) => {
        if (current) setLoaded({ page, data });
      },
      () => {},
    );
    return () => {
      current = false;
    };
  }, [slug, page]);

  const data = loaded?.data;

  return (
    <section id="reviews" aria-labelledby="reviews-heading" className="scroll-mt-28 pt-[90px]">
      <Container>
        <div className="flex flex-wrap items-end justify-between gap-4">
          <h2 id="reviews-heading" className="text-[34px] leading-[1.15] font-medium tracking-tight-3 tablet:text-[44px]">
            Reviews
          </h2>
          {data && data.reviewCount > 0 && (
            <p className="flex items-center gap-2 text-[16px] text-muted">
              <Stars rating={data.averageRating} />
              {data.averageRating}/5 from {data.reviewCount} {data.reviewCount === 1 ? "review" : "reviews"}
            </p>
          )}
        </div>
        <div className="mt-8 grid items-start gap-8 tablet:grid-cols-[1fr_380px]">
          <div>
            {data?.reviewCount === 0 && <p className="text-[16px] text-muted">No reviews yet. Be the first to share what you think.</p>}
            <ul className="flex flex-col gap-3">
              {data?.reviews.content.map((review) => (
                <li key={review.id} className="rounded-card bg-tile p-5">
                  <Stars rating={review.rating} />
                  <p className="mt-2 text-[18px] font-medium tracking-tight-2">{review.headline}</p>
                  <p className="mt-1 text-[16px] leading-[24px] text-muted">{review.body}</p>
                  <p className="mt-3 text-[14px] text-muted">
                    {review.authorName} · {new Date(review.createdAt).toLocaleDateString()}
                  </p>
                </li>
              ))}
            </ul>
            {data && data.reviews.totalPages > 1 && (
              <div className="mt-4 flex gap-2">
                <button type="button" disabled={page === 0} onClick={() => setPage(page - 1)} className={buttonClass("light", "disabled:opacity-40")}>
                  Newer
                </button>
                <button type="button" disabled={page + 1 >= data.reviews.totalPages} onClick={() => setPage(page + 1)} className={buttonClass("light", "disabled:opacity-40")}>
                  Older
                </button>
              </div>
            )}
          </div>
          <ReviewForm productId={productId} />
        </div>
      </Container>
    </section>
  );
}
