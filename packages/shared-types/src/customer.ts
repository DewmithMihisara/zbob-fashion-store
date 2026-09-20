export interface CartItem {
  id: number;
  variantId: number;
  productName: string;
  productSlug: string;
  imageUrl: string | null;
  size: string;
  color: string;
  quantity: number;
  unitPriceCents: number;
  lineTotalCents: number;
  available: boolean;
}

export interface Cart {
  items: CartItem[];
  subtotalCents: number;
}

export interface CheckoutLine {
  variantId: number;
  productName: string;
  productSlug: string;
  size: string;
  color: string;
  quantity: number;
  unitPriceCents: number;
  lineTotalCents: number;
  available: boolean;
}

export interface CheckoutQuote {
  lines: CheckoutLine[];
  subtotalCents: number;
  discountCents: number;
  shippingCents: number;
  totalCents: number;
  promotionCode: string | null;
  promotionError: string | null;
  shippingMethod: string | null;
  addressId: number | null;
  paymentAvailable: boolean;
  readyToPay: boolean;
  problems: string[];
}

export type ReturnStatus = "REQUESTED" | "APPROVED" | "REJECTED" | "COMPLETED";

export interface ReturnRequest {
  id: number;
  orderId: number;
  orderNumber: string | null;
  customerName: string | null;
  customerEmail: string | null;
  reason: string;
  status: ReturnStatus;
  staffNote: string | null;
  requestedAt: string;
  decidedAt: string | null;
}

export interface CustomerAddress {
  id: number;
  recipient: string;
  line1: string;
  line2: string | null;
  city: string;
  region: string | null;
  postalCode: string;
  country: string;
  phone: string | null;
}
