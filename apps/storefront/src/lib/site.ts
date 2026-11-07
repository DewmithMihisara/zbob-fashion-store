/** The public address of the storefront, used for links in search results and social previews. */
export const siteUrl = (process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000").replace(/\/+$/, "");

/** Business details shown across the site. Placeholders until zbob supplies real ones. */
export const site = {
  name: "zbob",
  tagline: "Considered clothing for modern, everyday living.",
  email: "hello@zbob.example",
  phone: "+00 000 000 000",
  location: "Your city, your country",
  announcement: "New season now in store",
} as const;

export const navLinks = [
  { href: "/", label: "Home" },
  { href: "/about", label: "About" },
  { href: "/shop", label: "Shop" },
  { href: "/blog", label: "Blog" },
  { href: "/contact", label: "Contact" },
] as const;

export const socialLinks = [
  { href: "https://instagram.com", label: "Instagram" },
  { href: "https://facebook.com", label: "Facebook" },
  { href: "https://x.com", label: "Twitter" },
  { href: "https://youtube.com", label: "Youtube" },
] as const;
