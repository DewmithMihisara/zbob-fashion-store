"use client";

import { useState } from "react";
import { niceTicks } from "./scale";
import { useWidth } from "./useWidth";

export interface Column {
  key: string;
  label: string;
  value: number;
}

const HEIGHT = 220;
const PAD = { top: 12, right: 8, bottom: 26, left: 56 };
const MAX_BAR = 24;

/** One series of columns from a shared baseline: thin bars, rounded tops, square bottoms, a tooltip on hover. */
export function ColumnChart({
  columns,
  format,
  tickFormat,
  label,
  integer = false,
}: {
  columns: Column[];
  format: (value: number) => string;
  tickFormat: (value: number) => string;
  label: string;
  /** Whole number ticks, for counts. */
  integer?: boolean;
}) {
  const { ref, width } = useWidth<HTMLDivElement>();
  const [hover, setHover] = useState<number | null>(null);
  const ticks = niceTicks(Math.max(0, ...columns.map((c) => c.value)), 4, integer);
  const top = ticks[ticks.length - 1]!;
  const plotWidth = Math.max(0, width - PAD.left - PAD.right);
  const plotHeight = HEIGHT - PAD.top - PAD.bottom;
  const band = columns.length ? plotWidth / columns.length : 0;
  const barWidth = Math.max(2, Math.min(MAX_BAR, band - 2));
  const y = (value: number) => PAD.top + plotHeight - (value / top) * plotHeight;
  // Label roughly every sixth day, so dates never collide.
  const labelEvery = Math.max(1, Math.ceil(columns.length / 6));
  const active = hover === null ? null : columns[hover];

  return (
    <div ref={ref} className="relative">
      {width > 0 && (
        <svg width={width} height={HEIGHT} role="img" aria-label={label}>
          {ticks.map((tick) => (
            <g key={tick}>
              <line x1={PAD.left} x2={width - PAD.right} y1={y(tick)} y2={y(tick)} stroke="var(--color-grid)" strokeWidth={1} />
              <text x={PAD.left - 8} y={y(tick)} dy="0.32em" textAnchor="end" className="fill-muted text-[11px]">
                {tickFormat(tick)}
              </text>
            </g>
          ))}
          {columns.map((column, i) => {
            const x = PAD.left + i * band + (band - barWidth) / 2;
            const h = Math.max(0, PAD.top + plotHeight - y(column.value));
            const r = Math.min(4, barWidth / 2, h);
            const base = PAD.top + plotHeight;
            // Rounded at the data end only; square where it meets the baseline.
            const path = h <= 0 ? "" : `M${x},${base} V${base - h + r} Q${x},${base - h} ${x + r},${base - h} H${x + barWidth - r} Q${x + barWidth},${base - h} ${x + barWidth},${base - h + r} V${base} Z`;
            return (
              <g key={column.key}>
                {path && <path d={path} fill="var(--color-series-1)" opacity={hover === null || hover === i ? 1 : 0.45} />}
                <rect
                  x={PAD.left + i * band}
                  y={PAD.top}
                  width={band}
                  height={plotHeight}
                  fill="transparent"
                  tabIndex={0}
                  aria-label={`${column.label}: ${format(column.value)}`}
                  onMouseEnter={() => setHover(i)}
                  onFocus={() => setHover(i)}
                  onMouseLeave={() => setHover(null)}
                  onBlur={() => setHover(null)}
                  className="outline-none"
                />
                {i % labelEvery === 0 && (
                  <text x={PAD.left + i * band + band / 2} y={HEIGHT - 8} textAnchor="middle" className="fill-muted text-[11px]">
                    {column.label}
                  </text>
                )}
              </g>
            );
          })}
        </svg>
      )}
      {active && hover !== null && (
        <div
          role="tooltip"
          className="pointer-events-none absolute top-0 z-10 rounded-lg border border-line bg-paper px-3 py-2 text-[12px] shadow-md"
          style={{ left: Math.min(Math.max(PAD.left + hover * band + band / 2 - 60, 0), Math.max(0, width - 130)) }}
        >
          <p className="text-muted">{active.label}</p>
          <p className="font-semibold text-ink">{format(active.value)}</p>
        </div>
      )}
    </div>
  );
}
