import type { Metadata } from "next";
import type { ReactNode } from "react";
import { CartProvider } from "@/components/cart/CartProvider";
import { WishlistProvider } from "@/components/wishlist/WishlistProvider";
import { AnnouncementBar } from "@/components/layout/AnnouncementBar";
import { Footer } from "@/components/layout/Footer";
import { Header } from "@/components/layout/Header";
import { inter, logoFace } from "./fonts";
import "./globals.css";

export const metadata: Metadata = {
  title: { default: "zbob", template: "%s | zbob" },
  description: "Modern clothing, made for everyday living.",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en" className={`${inter.variable} ${logoFace.variable}`}>
      <body>
        <CartProvider>
          <WishlistProvider>
          <div className="sticky top-0 z-40 -mb-[71px]">
            <AnnouncementBar />
            <Header />
          </div>
          <main>{children}</main>
          <Footer />
          </WishlistProvider>
        </CartProvider>
      </body>
    </html>
  );
}
