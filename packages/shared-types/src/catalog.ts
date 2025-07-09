export type ProductBadge = "NEW" | "BEST_SELLER";

export interface Category {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  badge: string | null;
  position: number;
}

export interface Brand {
  id: number;
  name: string;
  slug: string;
}

export interface ProductImage {
  id: number;
  url: string;
  altText: string | null;
  position: number;
}

export interface ProductVariant {
  id: number;
  sku: string;
  size: string;
  color: string;
  stockQuantity: number;
  lowStockThreshold: number;
  lowStock: boolean;
}

/** Compact shape used on product cards. */
export interface ProductSummary {
  id: number;
  name: string;
  slug: string;
  badge: ProductBadge | null;
  priceCents: number;
  compareAtPriceCents: number | null;
  imageUrl: string | null;
  categorySlug: string;
  inStock: boolean;
  published: boolean;
}

/** Storefront product page. Reports only whether it is in stock. */
export interface PublicProduct {
  id: number;
  name: string;
  slug: string;
  description: string;
  material: string | null;
  care: string | null;
  warranty: string | null;
  badge: ProductBadge | null;
  priceCents: number;
  compareAtPriceCents: number | null;
  inStock: boolean;
  category: Category;
  images: ProductImage[];
}

/** Full shape used by the admin product editor. */
export interface AdminProduct extends PublicProduct {
  published: boolean;
  brand: Brand | null;
  variants: ProductVariant[];
}

/** One row of the admin stock table. */
export interface InventoryRow {
  variantId: number;
  productId: number;
  productName: string;
  sku: string;
  size: string;
  color: string;
  stockQuantity: number;
  lowStockThreshold: number;
  lowStock: boolean;
}
