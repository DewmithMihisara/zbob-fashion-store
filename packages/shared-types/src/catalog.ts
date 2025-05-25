export type ProductBadge = "NEW" | "BEST_SELLER";

export interface Category {
  id: number;
  name: string;
  slug: string;
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
}

/** Full shape used on the product page and the admin editor. */
export interface Product extends Omit<ProductSummary, "imageUrl" | "categorySlug"> {
  description: string;
  material: string | null;
  care: string | null;
  warranty: string | null;
  published: boolean;
  category: Category;
  brand: Brand | null;
  images: ProductImage[];
  variants: ProductVariant[];
}
