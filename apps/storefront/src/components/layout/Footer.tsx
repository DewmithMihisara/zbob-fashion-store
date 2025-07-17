import Link from "next/link";
import { LinkButton } from "@/components/ui/Button";
import { MailIcon, PhoneIcon, PinIcon } from "@/components/ui/icons";
import { navLinks, site, socialLinks } from "@/lib/site";
import { Wordmark } from "./Wordmark";

const quickLinks = [...navLinks, { href: "/#reviews", label: "Reviews" }, { href: "/#styles", label: "Styles" }];

const contacts = [
  { icon: MailIcon, text: site.email, href: `mailto:${site.email}` },
  { icon: PhoneIcon, text: site.phone, href: `tel:${site.phone.replace(/\s/g, "")}` },
  { icon: PinIcon, text: site.location, href: null },
];

export function Footer() {
  return (
    <footer className="bg-ink text-paper">
      <div className="mx-auto max-w-[1190px] px-5 pt-16 pb-6 desktop:px-0">
        <div className="grid gap-12 tablet:grid-cols-[1fr_auto_auto_auto] tablet:gap-16">
          <div>
            <Wordmark className="text-[26px]" />
            <p className="mt-5 max-w-[320px] text-[16px] leading-[24px] font-medium text-paper/70">{site.tagline}</p>
            <LinkButton href="/contact" variant="light" className="mt-5">
              Contact zbob
            </LinkButton>
          </div>
          <FooterColumn title="Quick Links">
            {quickLinks.map((link) => (
              <Link key={link.href} href={link.href} className="text-[16px] font-medium text-paper/55 transition-colors hover:text-paper">
                {link.label}
              </Link>
            ))}
          </FooterColumn>
          <FooterColumn title="Follow us:">
            {socialLinks.map((link) => (
              <a key={link.label} href={link.href} target="_blank" rel="noreferrer" className="text-[16px] font-medium text-paper/55 transition-colors hover:text-paper">
                {link.label}
              </a>
            ))}
          </FooterColumn>
          <FooterColumn title="Get in touch">
            {contacts.map(({ icon: ContactIcon, text, href }) => {
              const body = (
                <>
                  <span className="flex size-8 items-center justify-center rounded-full border border-paper/20 text-paper">
                    <ContactIcon size={14} />
                  </span>
                  {text}
                </>
              );
              const style = "flex items-center gap-[10px] text-[16px] font-medium text-paper/55";
              return href ? (
                <a key={text} href={href} className={`${style} transition-colors hover:text-paper`}>
                  {body}
                </a>
              ) : (
                <span key={text} className={style}>
                  {body}
                </span>
              );
            })}
          </FooterColumn>
        </div>
        <p aria-hidden="true" className="mt-12 font-logo text-[33vw] leading-[0.85] uppercase select-none desktop:text-[430px]">
          zbob
        </p>
      </div>
    </footer>
  );
}

function FooterColumn({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div>
      <h2 className="text-[20px] font-medium tracking-tight-3">{title}</h2>
      <div className="mt-5 flex flex-col gap-[10px]">{children}</div>
    </div>
  );
}
