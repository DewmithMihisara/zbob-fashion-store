import type { Metadata } from "next";
import type { ReactNode } from "react";
import { inter, logoFace } from "./fonts";
import "./globals.css";

export const metadata: Metadata = {
  title: "zbob",
  description: "Modern clothing, made for everyday living.",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en" className={`${inter.variable} ${logoFace.variable}`}>
      <body>{children}</body>
    </html>
  );
}
