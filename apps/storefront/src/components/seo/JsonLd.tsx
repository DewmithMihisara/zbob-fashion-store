import { serializeJsonLd } from "@/lib/structured-data";

export function JsonLd({ data }: { data: Record<string, unknown> }) {
  // Safe: serializeJsonLd escapes "<", so the content can not end the script element.
  return <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: serializeJsonLd(data) }} />;
}
