import type { Metadata } from "next";
import { ContactForm } from "@/components/contact/ContactForm";
import { CommunitySection } from "@/components/home/CommunitySection";
import { LinkButton } from "@/components/ui/Button";
import { MailIcon, PhoneIcon, PinIcon } from "@/components/ui/icons";
import { PageHero } from "@/components/ui/PageHero";
import { Photo } from "@/components/ui/Photo";
import { Container } from "@/components/ui/SectionHeading";
import { getSiteMedia } from "@/lib/api";
import { site } from "@/lib/site";

export const dynamic = "force-dynamic";

export const metadata: Metadata = {
  title: "Contact",
  description: "Get in touch with zbob.",
};

const CARDS = [
  { icon: MailIcon, value: site.email, label: "Email Address" },
  { icon: PhoneIcon, value: site.phone, label: "Phone Number" },
  { icon: PinIcon, value: site.location, label: "Location" },
];

export default async function ContactPage() {
  const media = await getSiteMedia();
  return (
    <>
      <PageHero
        lead="Contact"
        label="Here to help"
        photo={media["contact-hero"]}
        title={
          <>
            Tell us how we
            <br />
            can help you
          </>
        }
        text="Questions about an item, your size or anything else? Send us a message and we will reply."
      >
        <LinkButton href="/shop" variant="light">
          Browse collections
        </LinkButton>
        <LinkButton href="/about" variant="glass">
          About us
        </LinkButton>
      </PageHero>
      <section className="pt-[86px]">
        <Container>
          <div className="grid gap-[15px] tablet:grid-cols-3">
            {CARDS.map(({ icon: CardIcon, value, label }) => (
              <div key={label} className="flex flex-col items-center rounded-card bg-tile px-4 py-[22px] text-center">
                <span className="flex size-[60px] items-center justify-center rounded-[10px] bg-canvas">
                  <CardIcon size={26} />
                </span>
                <p className="mt-7 text-[22px] font-medium tracking-tight-3">{value}</p>
                <p className="mt-1 text-[18px] font-medium text-muted">{label}</p>
              </div>
            ))}
          </div>
          <div className="mt-4 grid gap-4 tablet:grid-cols-2">
            <div className="relative min-h-[420px] overflow-hidden rounded-card bg-tile">
              <Photo src={media["contact-side"]} alt="" sizes="(min-width: 810px) 50vw, 100vw" />
            </div>
            <ContactForm />
          </div>
        </Container>
      </section>
      <CommunitySection media={media} />
    </>
  );
}
