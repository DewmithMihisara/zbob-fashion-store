import { Suspense } from "react";
import { ExportButton } from "@/components/ui/ExportButton";
import { PageHeader } from "@/components/ui/PageHeader";
import { InventoryTable } from "@/features/inventory/InventoryTable";

export default function InventoryPage() {
  return (
    <>
      <PageHeader
        title="Stock"
        description="Every size and colour of every product. Change a number and save, or tick rows to change many at once."
        action={<ExportButton name="inventory" />}
      />
      <Suspense>
        <InventoryTable />
      </Suspense>
    </>
  );
}
