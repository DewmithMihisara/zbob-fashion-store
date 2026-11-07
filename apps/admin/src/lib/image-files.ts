const ALLOWED_TYPES = ["image/jpeg", "image/png", "image/webp"];
const MAX_BYTES = 10 * 1024 * 1024;

/** Gives a quick answer before uploading. The API checks the file contents again. */
export function imageFileProblem(file: Pick<File, "name" | "type" | "size">): string | null {
  if (!ALLOWED_TYPES.includes(file.type)) {
    return `${file.name}: only JPEG, PNG and WebP images are allowed.`;
  }
  if (file.size > MAX_BYTES) {
    return `${file.name}: images can be at most 10 MB.`;
  }
  return null;
}
