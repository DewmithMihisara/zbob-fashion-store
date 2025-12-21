import type { Metadata } from "next";
import { CartView } from "@/components/cart/CartView";
import { Container, Heading } from "@/components/ui/SectionHeading";

export const metadata: Metadata = { title: "Cart", robots: { index: false, follow: false } };

export default function CartPage() {
  return (
    <section className="pt-[130px] pb-20">
      <Container>
        <Heading className="mb-8">Your cart</Heading>
        <CartView />
      </Container>
    </section>
  );
}
