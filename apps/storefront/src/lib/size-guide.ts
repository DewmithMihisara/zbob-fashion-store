export type SizeGuideBlock = { type: "table"; rows: string[][] } | { type: "text"; text: string };

/**
 * Staff write the guide as plain text. Lines containing "|" form a table, with the first such line as
 * its header; other lines are paragraphs. Rendered as text, so nothing typed can run as HTML.
 */
export function parseSizeGuide(guide: string): SizeGuideBlock[] {
  const blocks: SizeGuideBlock[] = [];
  for (const raw of guide.split("\n")) {
    const line = raw.trim();
    if (!line) {
      continue;
    }
    const last = blocks[blocks.length - 1];
    if (line.includes("|")) {
      const cells = line.split("|").map((cell) => cell.trim());
      if (last?.type === "table") {
        last.rows.push(cells);
      } else {
        blocks.push({ type: "table", rows: [cells] });
      }
    } else {
      blocks.push({ type: "text", text: line });
    }
  }
  return blocks;
}
