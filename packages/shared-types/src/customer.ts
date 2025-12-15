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
