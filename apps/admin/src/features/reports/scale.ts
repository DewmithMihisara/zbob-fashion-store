/** Round tick values from zero up past the largest value, e.g. 0, 2,000, 4,000, 6,000. */
export function niceTicks(max: number, count = 4): number[] {
  if (max <= 0) {
    return [0, 1];
  }
  const rough = max / count;
  const magnitude = 10 ** Math.floor(Math.log10(rough));
  const step = [1, 2, 2.5, 5, 10].map((m) => m * magnitude).find((s) => s >= rough)!;
  const ticks: number[] = [];
  for (let value = 0; value < max + step; value += step) {
    ticks.push(Math.round(value * 1e6) / 1e6);
    if (value >= max) {
      break;
    }
  }
  return ticks;
}

/** Full numbers below 10,000, then compact: 950, 4,200, 12K, 1.5M. */
export function compactNumber(value: number): string {
  if (Math.abs(value) < 10_000) {
    return value.toLocaleString("en-US");
  }
  return new Intl.NumberFormat("en-US", { notation: "compact", maximumFractionDigits: 1 }).format(value);
}

export function compactMoney(cents: number): string {
  return `$${compactNumber(Math.round(cents / 100))}`;
}
