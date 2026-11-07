import type { SiteMedia } from "@zbob/shared-types";
import { SparkleIcon } from "@/components/ui/icons";
import { Photo } from "@/components/ui/Photo";
import { Reveal } from "@/components/ui/Reveal";
import { CentredHeading, Container } from "@/components/ui/SectionHeading";

const FEATURES = [
  { title: "All-day comfort", text: "Cut to feel natural on the body, from your first coffee to the end of a long day.", tags: ["All-day", "Comfort", "Easy fit"] },
  { title: "Clean silhouettes", text: "Modern shapes with enough structure to look sharp and enough ease to stay relaxed.", tags: ["Balanced", "Modern", "Shaped"] },
  { title: "Simple styling", text: "Pieces designed to work together, so choosing an outfit takes moments, not minutes.", tags: ["Versatile", "Simple", "Layered"] },
  { title: "Daily essentials", text: "The core pieces you reach for most, made to take regular wear and frequent washing.", tags: ["Core", "Everyday", "Durable"] },
  { title: "Honest design", text: "Every pocket, seam and fastening is there for a reason, and nothing is there for show.", tags: ["Practical", "Useful", "Adaptable"] },
  { title: "The zbob look", text: "Clean lines and quiet colour that look just as good next year as they do this year.", tags: ["Clean", "Minimal", "Timeless"] },
];

export function FeaturesSection({ media }: { media: SiteMedia }) {
  return (
    <section id="styles" className="scroll-mt-24 pt-[90px]">
      <Container>
        <CentredHeading title="Good style, less effort" intro="Careful design that brings together modern shapes, comfort and versatility for everyday life." />
        <div className="mt-[55px] grid gap-5 tablet:grid-cols-2 desktop:grid-cols-3">
          {FEATURES.map((feature, index) => {
            const n = index + 1;
            return (
              <Reveal key={feature.title} delay={(index % 3) * 0.06} className="rounded-card bg-paper p-5">
                <div className="relative mx-auto h-[220px] w-[240px]">
                  <div className="absolute top-[12px] left-[6px] h-[180px] w-[125px] -rotate-[8deg] overflow-hidden rounded-card border-2 border-paper shadow-lg">
                    <Photo src={media[`feature-${n}-back`]} alt="" sizes="125px" />
                  </div>
                  <div className="absolute top-[6px] right-[6px] h-[185px] w-[135px] rotate-[8deg] overflow-hidden rounded-card border-2 border-paper shadow-xl">
                    <Photo src={media[`feature-${n}-front`]} alt="" sizes="135px" />
                  </div>
                </div>
                <h3 className="mt-2 border-b border-line pb-3 text-[24px] font-medium tracking-tight-3">{feature.title}</h3>
                <p className="mt-3 text-[16px] leading-[25px] font-medium text-muted">{feature.text}</p>
                <ul className="mt-5 flex flex-wrap gap-2">
                  {feature.tags.map((tag) => (
                    <li key={tag} className="inline-flex items-center gap-[5px] rounded-full bg-canvas px-[10px] py-[5px] text-[14px]">
                      <SparkleIcon size={13} />
                      {tag}
                    </li>
                  ))}
                </ul>
              </Reveal>
            );
          })}
        </div>
      </Container>
    </section>
  );
}
