import { Inter, Roboto_Condensed } from "next/font/google";

export const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

export const logoFace = Roboto_Condensed({
  subsets: ["latin"],
  weight: "600",
  variable: "--font-logo-face",
  display: "swap",
});
