export const MAX_PRODUCT_PHOTOS = 5;

/** Answers before uploading anything, so a selection is never left half uploaded. The API enforces this too. */
export function photoLimitProblem(current: number, adding: number): string | null {
  if (current + adding <= MAX_PRODUCT_PHOTOS) {
    return null;
  }
  const room = MAX_PRODUCT_PHOTOS - current;
  return `A product can have at most ${MAX_PRODUCT_PHOTOS} photos. You can add ${room} more.`;
}
