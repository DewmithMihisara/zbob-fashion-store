import type { Metadata } from "next";
import { Suspense } from "react";
import { CheckoutView } from "@/components/checkout/CheckoutView";
import { Container, Heading } from "@/components/ui/SectionHeading";

export const metadata: Metadata = { title: "Checkout", robots: { index: false, follow: false } };

export default function CheckoutPage() {
  return (
    <section className="pt-[130px] pb-20">
      <Container>
        <Heading className="mb-8">Checkout</Heading>
        <Suspense>
          <CheckoutView />
        </Suspense>
      </Container>
    </section>
  );
}
