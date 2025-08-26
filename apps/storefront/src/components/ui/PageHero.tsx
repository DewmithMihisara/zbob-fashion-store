import type { ReactNode } from "react";
import { HeroEyebrow } from "./Eyebrow";
import { Photo } from "./Photo";

export function PageHero({
  lead,
  label,
  title,
  text,
  photo,
  children,
}: {
  lead: string;
  label: string;
  title: ReactNode;
  text: string;
  photo: string | null | undefined;
  children?: ReactNode;
}) {
  return (
    <section className="relative flex min-h-[560px] flex-col items-center overflow-hidden bg-ink px-5 pt-[116px] pb-16 text-center text-paper">
      <Photo src={photo} alt="" sizes="100vw" tone="dark" priority />
      <div className="absolute inset-0 bg-gradient-to-b from-black/30 via-black/25 to-black/85" />
      <div className="relative flex flex-col items-center">
        <HeroEyebrow lead={lead}>{label}</HeroEyebrow>
        <h1 className="mt-5 max-w-[720px] text-[42px] leading-[1.13] font-medium tracking-tight-3 tablet:text-[62px]">{title}</h1>
        <p className="mt-4 max-w-[480px] text-[18px] leading-[27px] font-medium text-paper/75">{text}</p>
        <div className="mt-6 flex flex-wrap items-center justify-center gap-3">{children}</div>
      </div>
    </section>
  );
}
