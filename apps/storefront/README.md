# zbob storefront

The customer website. It renders each page on the server from the API, so changes made in the admin dashboard show on the next page load.

Start it from the repository root with `pnpm dev:storefront`. See `.env.example` for the variables.

- Business details are in `src/lib/site.ts`.
- Colours, fonts and breakpoints are in `src/app/globals.css`.
- `src/components/product/OrderNowButton.tsx` is where checkout connects once a payment gateway is chosen.
