import { Suspense } from "react";
import { PageHeader } from "@/components/ui/PageHeader";
import { InventoryTable } from "@/features/inventory/InventoryTable";

export default function InventoryPage() {
  return (
    <>
      <PageHeader title="Stock" description="Every size and colour of every product. Change a number and save." />
      <Suspense>
        <InventoryTable />
      </Suspense>
    </>
  );
}
