import type { SiteMedia } from "@zbob/shared-types";
import { LinkButton } from "@/components/ui/Button";
import { SectionEyebrow } from "@/components/ui/Eyebrow";
import { GlobeIcon } from "@/components/ui/icons";
import { Photo } from "@/components/ui/Photo";
import { CentredHeading } from "@/components/ui/SectionHeading";

/** Width, height and tilt of each photo, from the outer edges of the arc to the centre. */
const ARC = [
  { width: 268, height: 400, tilt: 42 },
  { width: 178, height: 268, tilt: 28 },
  { width: 133, height: 196, tilt: 14 },
  { width: 122, height: 172, tilt: 0 },
  { width: 133, height: 196, tilt: -14 },
  { width: 178, height: 268, tilt: -28 },
  { width: 268, height: 400, tilt: -42 },
];

export function CommunitySection({ media }: { media: SiteMedia }) {
  return (
    <section className="overflow-hidden pt-[120px] pb-16">
      <div className="px-5">
        <CentredHeading
          eyebrow={<SectionEyebrow icon={<GlobeIcon size={14} />}>Stay connected</SectionEyebrow>}
          title={
            <>
              See how people
              <br />
              wear zbob every day
            </>
          }
          intro="Follow along on social media for new looks, styling ideas and pieces from our community."
        />
        <div className="mt-6 flex flex-wrap justify-center gap-[12px]">
          <LinkButton href="/shop">See collections</LinkButton>
          <LinkButton href="/contact" variant="light">
            Contact us
          </LinkButton>
        </div>
      </div>
      <div className="mt-10 flex items-center justify-center gap-6 [perspective:1100px]">
        {ARC.map((item, index) => (
          <div
            key={index}
            style={{ width: item.width, height: item.height, transform: `rotateY(${item.tilt}deg)` }}
            className={`relative shrink-0 overflow-hidden rounded-[18px] ${index === 0 || index === 6 ? "hidden desktop:block" : ""}`}
          >
            <Photo src={media[`community-${index + 1}`]} alt="" sizes="270px" />
          </div>
        ))}
      </div>
    </section>
  );
}
