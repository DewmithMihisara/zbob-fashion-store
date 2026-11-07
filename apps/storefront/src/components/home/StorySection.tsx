import { LinkButton } from "@/components/ui/Button";
import { HeroEyebrow } from "@/components/ui/Eyebrow";
import { Photo } from "@/components/ui/Photo";

export function StorySection({ photo }: { photo: string | null | undefined }) {
  return (
    <section className="relative mt-[90px] flex min-h-[620px] items-center justify-center overflow-hidden bg-ink text-paper tablet:min-h-[800px]">
      <Photo src={photo} alt="" sizes="100vw" tone="dark" />
      <div className="absolute inset-0 bg-black/35" />
      <div className="relative flex flex-col items-center px-5 text-center">
        <HeroEyebrow lead="zbob">Our story</HeroEyebrow>
        <h2 className="mt-5 text-[38px] leading-[1.15] font-medium tracking-tight-3 tablet:text-[48px]">Made to be worn, often</h2>
        <p className="mt-4 max-w-[380px] text-[16px] leading-[25px] font-medium text-paper/75">
          zbob began with a simple idea: clothes should earn their place in your week. We make pieces that are easy to wear and built to last.
        </p>
        <div className="mt-6 flex flex-wrap justify-center gap-[12px]">
          <LinkButton href="/about" variant="light">
            More about us
          </LinkButton>
          <LinkButton href="/contact" variant="glass">
            Contact us
          </LinkButton>
        </div>
      </div>
    </section>
  );
}
