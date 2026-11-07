import { StarIcon } from "@/components/ui/icons";

export function Stars({ rating, size = 15 }: { rating: number; size?: number }) {
  const rounded = Math.round(rating);
  return (
    <span className="inline-flex gap-[2px] text-accent" role="img" aria-label={`${rating} out of 5 stars`}>
      {[1, 2, 3, 4, 5].map((star) => (
        <StarIcon key={star} size={size} className={star <= rounded ? "" : "opacity-25"} />
      ))}
    </span>
  );
}
