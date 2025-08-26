"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { LinkButton } from "@/components/ui/Button";
import { PlusIcon, SearchIcon } from "@/components/ui/icons";
import { navLinks } from "@/lib/site";
import { Wordmark } from "./Wordmark";

/** Pages that open with a full-bleed photo the header sits over until scrolled. */
const HERO_PAGES = new Set(["/", "/shop", "/about", "/blog", "/contact"]);

export function Header() {
  const pathname = usePathname();
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 40);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  const overHero = HERO_PAGES.has(pathname) && !scrolled && !menuOpen;

  return (
    <header
      className={`h-[71px] transition-colors duration-300 ${
        overHero ? "bg-transparent text-paper" : "bg-canvas text-ink"
      }`}
    >
      <div className="mx-auto flex h-full max-w-[1150px] items-center justify-between px-5 desktop:px-0">
        <Wordmark />
        <nav aria-label="Main" className="hidden items-center gap-6 tablet:flex">
          {navLinks.map((link) => (
            <Link key={link.href} href={link.href} className="text-[16px] font-medium tracking-tight-2 transition-opacity hover:opacity-60">
              {link.label}
            </Link>
          ))}
        </nav>
        <div className="flex items-center gap-[10px]">
          <Link
            href="/shop"
            aria-label="Search products"
            className={`flex size-[37px] items-center justify-center rounded-full ${overHero ? "bg-paper/25 backdrop-blur-md" : "bg-paper"}`}
          >
            <SearchIcon />
          </Link>
          <LinkButton href="/shop" variant={overHero ? "light" : "dark"} className="hidden tablet:inline-flex">
            Shop all items
          </LinkButton>
          <button
            type="button"
            aria-label={menuOpen ? "Close menu" : "Open menu"}
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen((open) => !open)}
            className={`flex size-[37px] items-center justify-center rounded-full tablet:hidden ${overHero ? "bg-paper/25 backdrop-blur-md" : "bg-paper"}`}
          >
            <PlusIcon size={20} className={`transition-transform ${menuOpen ? "rotate-45" : ""}`} />
          </button>
        </div>
      </div>
      {menuOpen && (
        <nav aria-label="Mobile" className="flex flex-col gap-1 bg-canvas px-5 pt-2 pb-6 text-ink shadow-lg tablet:hidden">
          {navLinks.map((link) => (
            <Link key={link.href} href={link.href} onClick={() => setMenuOpen(false)} className="py-2 text-[20px] font-medium tracking-tight-2">
              {link.label}
            </Link>
          ))}
        </nav>
      )}
    </header>
  );
}
