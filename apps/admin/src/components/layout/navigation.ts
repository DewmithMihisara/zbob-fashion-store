import type { Role } from "@zbob/shared-types";

export interface NavItem {
  href: string;
  label: string;
  /** Only these roles see the link. All back office roles when omitted. */
  roles?: Role[];
}

export interface NavGroup {
  title: string;
  items: NavItem[];
}

export const navigation: NavGroup[] = [
  {
    title: "Overview",
    items: [
      { href: "/dashboard", label: "Dashboard" },
      { href: "/activity", label: "Activity", roles: ["ADMIN"] },
    ],
  },
  {
    title: "Catalog",
    items: [
      { href: "/products", label: "Products" },
      { href: "/inventory", label: "Stock" },
      { href: "/categories", label: "Categories" },
      { href: "/brands", label: "Brands" },
    ],
  },
  {
    title: "Website",
    items: [
      { href: "/site-photos", label: "Site photos" },
      { href: "/blog", label: "Blog" },
      { href: "/reviews", label: "Reviews" },
    ],
  },
  {
    title: "Sales",
    items: [
      { href: "/orders", label: "Orders" },
      { href: "/promotions", label: "Promotions" },
      { href: "/shipping", label: "Shipping" },
    ],
  },
  {
    title: "People",
    items: [
      { href: "/customers", label: "Customers" },
      { href: "/messages", label: "Messages" },
      { href: "/subscribers", label: "Subscribers" },
      { href: "/staff", label: "Staff", roles: ["ADMIN"] },
    ],
  },
];

export function navigationFor(role: Role): NavGroup[] {
  return navigation
    .map((group) => ({ ...group, items: group.items.filter((item) => !item.roles || item.roles.includes(role)) }))
    .filter((group) => group.items.length > 0);
}
