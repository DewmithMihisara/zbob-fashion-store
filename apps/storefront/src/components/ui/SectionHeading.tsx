import type { ReactNode } from "react";

export function Heading({ children, className = "" }: { children: ReactNode; className?: string }) {
  return (
    <h2 className={`text-[34px] leading-[1.15] font-medium tracking-tight-3 tablet:text-[48px] ${className}`}>{children}</h2>
  );
}

/** Left heading with its action on the right, as used above product grids. */
export function SplitHeading({ eyebrow, title, action }: { eyebrow?: ReactNode; title: ReactNode; action?: ReactNode }) {
  return (
    <div className="flex flex-col gap-5 tablet:flex-row tablet:items-end tablet:justify-between">
      <div className="flex flex-col items-start gap-4">
        {eyebrow}
        <Heading>{title}</Heading>
      </div>
      {action}
    </div>
  );
}

/** Centred heading with an intro line. */
export function CentredHeading({ eyebrow, title, intro }: { eyebrow?: ReactNode; title: ReactNode; intro?: ReactNode }) {
  return (
    <div className="flex flex-col items-center gap-4 text-center">
      {eyebrow}
      <Heading>{title}</Heading>
      {intro && <p className="max-w-[440px] text-[16px] leading-[25px] font-medium text-muted">{intro}</p>}
    </div>
  );
}

export function Container({ children, className = "" }: { children: ReactNode; className?: string }) {
  return <div className={`mx-auto w-full max-w-[1150px] px-5 desktop:px-0 ${className}`}>{children}</div>;
}
