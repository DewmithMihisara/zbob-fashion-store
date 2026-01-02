export type Role = "ADMIN" | "STAFF" | "CUSTOMER";

export interface AuthResponse {
  accessToken: string;
  expiresInSeconds: number;
  role: Role;
  fullName: string;
}

export interface DashboardSummary {
  products: number;
  publishedProducts: number;
  lowStockVariants: number;
  outOfStockVariants: number;
  pendingReviews: number;
  unhandledMessages: number;
  ordersAwaitingPayment: number;
  customers: number;
}

export interface UserAccount {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  active: boolean;
  createdAt: string;
}

export type DiscountType = "PERCENT" | "FIXED";

export interface Promotion {
  id: number;
  code: string;
  description: string | null;
  discountType: DiscountType;
  discountValue: number;
  minimumCents: number;
  startsAt: string | null;
  endsAt: string | null;
  active: boolean;
}

export interface ShippingMethod {
  id: number;
  name: string;
  description: string | null;
  priceCents: number;
  estimatedDays: number;
  active: boolean;
}

export type OrderStatus = "PENDING_PAYMENT" | "PAID" | "SHIPPED" | "DELIVERED" | "CANCELLED";

export interface OrderSummary {
  id: number;
  orderNumber: string;
  status: OrderStatus;
  totalCents: number;
  recipient: string;
  createdAt: string;
}

export interface OrderItem {
  id: number;
  productName: string;
  sku: string;
  size: string;
  color: string;
  unitPriceCents: number;
  quantity: number;
}

export interface OrderDetail extends OrderSummary {
  subtotalCents: number;
  discountCents: number;
  shippingCents: number;
  promotionCode: string | null;
  shippingMethod: string;
  line1: string;
  line2: string | null;
  city: string;
  region: string | null;
  postalCode: string;
  country: string;
  phone: string | null;
  items: OrderItem[];
}

export interface ContactMessage {
  id: number;
  name: string;
  email: string;
  subject: string | null;
  message: string;
  handled: boolean;
  receivedAt: string;
}

export interface ActivityEntry {
  id: number;
  actor: string;
  action: string;
  summary: string;
  status: number;
  at: string;
}

export interface Subscriber {
  id: number;
  email: string;
  subscribedAt: string;
}
