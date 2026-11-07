export type Block =
  | { type: "heading"; text: string }
  | { type: "paragraph"; text: string }
  | { type: "list"; items: string[] };

/**
 * Splits post text into blocks. "## " starts a heading and "- " a list item.
 * The output is rendered as text, so markup typed into a post can never run as HTML.
 */
export function parsePostBody(body: string): Block[] {
  const blocks: Block[] = [];
  for (const raw of body.split("\n")) {
    const line = raw.trim();
    if (!line) {
      continue;
    }
    const last = blocks[blocks.length - 1];
    if (line.startsWith("## ")) {
      blocks.push({ type: "heading", text: line.slice(3) });
    } else if (line.startsWith("- ")) {
      if (last?.type === "list") {
        last.items.push(line.slice(2));
      } else {
        blocks.push({ type: "list", items: [line.slice(2)] });
      }
    } else {
      blocks.push({ type: "paragraph", text: line });
    }
  }
  return blocks;
}
