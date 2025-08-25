"use client";

import type { ReviewSummary } from "@zbob/shared-types";
import { useState } from "react";
import { StarIcon } from "@/components/ui/icons";
import { CentredHeading, Container } from "@/components/ui/SectionHeading";

/** Shows only approved reviews; the score comes from them and the section hides when there are none. */
export function ReviewsSection({ summary }: { summary: ReviewSummary }) {
  const [active, setActive] = useState(0);
  const reviews = summary.reviews.slice(0, 5);
  const review = reviews[active];
  if (!review) {
    return null;
  }

  return (
    <section id="reviews" className="scroll-mt-24 pt-[90px]">
      <Container>
        <CentredHeading
          title="In our customers' words"
          intro="What people say about the fabrics, the fit and how the clothes hold up once they are part of the week."
        />
        <div className="mx-auto mt-[55px] max-w-[950px] overflow-hidden rounded-card bg-paper">
          <div className="p-[18px]">
            <span className="inline-flex items-center gap-2 rounded-full bg-canvas px-[14px] py-[7px] text-[14px] text-muted">
              <span className="flex gap-[3px] text-accent" aria-hidden="true">
                {[1, 2, 3, 4, 5].map((star) => (
                  <StarIcon key={star} size={15} className={star <= Math.round(summary.averageRating) ? "" : "opacity-25"} />
                ))}
              </span>
              {summary.averageRating}/5 from {summary.reviewCount} {summary.reviewCount === 1 ? "review" : "reviews"}
            </span>
          </div>
          <figure className="mx-auto flex min-h-[320px] max-w-[580px] flex-col items-center justify-center px-5 pb-12 text-center">
            <blockquote className="text-[17px] leading-[25px]">
              <p className="font-medium">{review.headline}</p>
              <p className="mt-2 text-muted">{review.body}</p>
            </blockquote>
            <figcaption className="mt-8">
              <p className="text-[18px]">{review.authorName}</p>
              {review.authorTitle && <p className="text-[14px] text-muted">{review.authorTitle}</p>}
            </figcaption>
          </figure>
          {reviews.length > 1 && (
            <div className="grid auto-cols-fr grid-flow-col gap-1">
              {reviews.map((item, index) => (
                <button
                  key={item.id}
                  type="button"
                  aria-pressed={index === active}
                  onClick={() => setActive(index)}
                  className={`truncate px-2 py-[30px] text-[15px] font-medium transition-colors ${
                    index === active ? "bg-ink text-paper" : "bg-tile text-ink hover:bg-line"
                  }`}
                >
                  {item.authorName}
                </button>
              ))}
            </div>
          )}
        </div>
      </Container>
    </section>
  );
}
