import type { ReactNode } from "react";

export function Table({ headers, children }: { headers: string[]; children: ReactNode }) {
  return (
    <div className="overflow-x-auto rounded-xl border border-line bg-paper">
      <table className="w-full text-left text-[13px]">
        <thead className="border-b border-line text-[12px] text-muted">
          <tr>
            {headers.map((header) => (
              <th key={header} scope="col" className="px-4 py-3 font-medium whitespace-nowrap">
                {header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-line">{children}</tbody>
      </table>
    </div>
  );
}

export function Cell({ children, className = "" }: { children?: ReactNode; className?: string }) {
  return <td className={`px-4 py-3 align-middle ${className}`}>{children}</td>;
}

export function EmptyRow({ columns, children }: { columns: number; children: ReactNode }) {
  return (
    <tr>
      <td colSpan={columns} className="px-4 py-10 text-center text-muted">
        {children}
      </td>
    </tr>
  );
}
