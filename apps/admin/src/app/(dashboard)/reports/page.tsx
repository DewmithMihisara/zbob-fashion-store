"use client";

import type { SalesReport } from "@zbob/shared-types";
import { formatPrice } from "@zbob/shared-utils";
import { useState, type ReactNode } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { PageHeader } from "@/components/ui/PageHeader";
import { Cell, Table } from "@/components/ui/Table";
import { BestSellers } from "@/features/reports/BestSellers";
import { ColumnChart } from "@/features/reports/ColumnChart";
import { compactMoney, compactNumber } from "@/features/reports/scale";
import { StockHealth } from "@/features/reports/StockHealth";
import { useData } from "@/hooks/useData";

const RANGES = [7, 30, 90];

function dayLabel(iso: string): string {
  return new Date(`${iso}T00:00:00`).toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

function Panel({ title, note, children }: { title: string; note?: string; children: ReactNode }) {
  return (
    <section className="rounded-xl border border-line bg-paper p-5">
      <h2 className="text-[15px] font-semibold">{title}</h2>
      {note && <p className="mb-3 text-[12px] text-muted">{note}</p>}
      <div className={note ? "" : "mt-3"}>{children}</div>
    </section>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-line bg-paper p-5">
      <p className="text-[13px] text-muted">{label}</p>
      <p className="mt-1 text-[28px] font-semibold tracking-tight">{value}</p>
    </div>
  );
}

export default function ReportsPage() {
  const [days, setDays] = useState(30);
  const [asTable, setAsTable] = useState(false);
  const { data, error } = useData<SalesReport>(`/admin/reports/sales?days=${days}`);
  const daily = data?.daily ?? [];

  return (
    <>
      <PageHeader title="Reports" description="Revenue counts paid, shipped and delivered orders. Cancelled orders are left out." />
      <div className="mb-5 flex flex-wrap items-center gap-2" role="group" aria-label="Time range">
        {RANGES.map((range) => (
          <Button key={range} variant={range === days ? "primary" : "secondary"} aria-pressed={range === days} onClick={() => setDays(range)}>
            Last {range} days
          </Button>
        ))}
        <span className="ml-auto">
          <Button variant="secondary" aria-pressed={asTable} onClick={() => setAsTable(!asTable)}>
            {asTable ? "Show charts" : "Show as table"}
          </Button>
        </span>
      </div>
      <ErrorAlert message={error} />
      {data && (
        <div className="flex flex-col gap-4">
          <div className="grid gap-4 sm:grid-cols-3">
            <Stat label="Orders" value={compactNumber(data.orders)} />
            <Stat label="Revenue" value={formatPrice(data.revenueCents)} />
            <Stat label="Average paid order" value={formatPrice(data.averageOrderCents)} />
          </div>
          {asTable ? (
            <Table headers={["Day", "Orders", "Revenue"]}>
              {daily.map((day) => (
                <tr key={day.date}>
                  <Cell>{dayLabel(day.date)}</Cell>
                  <Cell>{day.orders}</Cell>
                  <Cell>{formatPrice(day.revenueCents)}</Cell>
                </tr>
              ))}
            </Table>
          ) : (
            <div className="grid gap-4 lg:grid-cols-2">
              <Panel title="Revenue by day" note={`Days run in ${data.timeZone} time.`}>
                <ColumnChart
                  label={`Revenue by day, last ${data.days} days`}
                  columns={daily.map((d) => ({ key: d.date, label: dayLabel(d.date), value: d.revenueCents }))}
                  format={formatPrice}
                  tickFormat={compactMoney}
                />
              </Panel>
              <Panel title="Orders by day" note="Includes orders still waiting for payment.">
                <ColumnChart
                  label={`Orders by day, last ${data.days} days`}
                  columns={daily.map((d) => ({ key: d.date, label: dayLabel(d.date), value: d.orders }))}
                  format={(v) => `${v} ${v === 1 ? "order" : "orders"}`}
                  tickFormat={compactNumber}
                  integer
                />
              </Panel>
            </div>
          )}
          <div className="grid gap-4 lg:grid-cols-2">
            <Panel title="Best sellers" note="Units sold in this period.">
              <BestSellers items={data.bestSellers} />
            </Panel>
            <Panel title="Stock right now" note="Every size and colour of every product.">
              <StockHealth stock={data.stock} />
            </Panel>
          </div>
        </div>
      )}
    </>
  );
}
