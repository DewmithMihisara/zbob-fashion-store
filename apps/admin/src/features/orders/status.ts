import type { OrderStatus } from "@zbob/shared-types";

export const statusLabels: Record<OrderStatus, string> = {
  PENDING_PAYMENT: "Awaiting payment",
  PAID: "Paid",
  SHIPPED: "Shipped",
  DELIVERED: "Delivered",
  CANCELLED: "Cancelled",
};

export const statusTones: Record<OrderStatus, "neutral" | "good" | "warn" | "bad"> = {
  PENDING_PAYMENT: "warn",
  PAID: "good",
  SHIPPED: "good",
  DELIVERED: "neutral",
  CANCELLED: "bad",
};

/** Mirrors OrderStatus.canMoveTo in the API, which has the final say. */
export const nextStatuses: Record<OrderStatus, OrderStatus[]> = {
  PENDING_PAYMENT: ["PAID", "CANCELLED"],
  PAID: ["SHIPPED", "CANCELLED"],
  SHIPPED: ["DELIVERED"],
  DELIVERED: [],
  CANCELLED: [],
};
