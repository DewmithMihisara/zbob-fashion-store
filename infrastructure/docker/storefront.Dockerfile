# Build from the repository root:
#   docker build -f infrastructure/docker/storefront.Dockerfile \
#     --build-arg NEXT_PUBLIC_API_URL=https://api.example.com/api/v1 -t zbob-storefront .
FROM node:24-slim AS build
RUN corepack enable pnpm
WORKDIR /src
COPY pnpm-lock.yaml pnpm-workspace.yaml package.json ./
COPY packages ./packages
COPY apps/storefront ./apps/storefront
RUN pnpm install --frozen-lockfile --filter @zbob/storefront...
ARG NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
RUN pnpm --filter @zbob/storefront build

FROM node:24-slim
WORKDIR /app
ENV NODE_ENV=production PORT=3000 HOSTNAME=0.0.0.0
COPY --from=build /src/apps/storefront/.next/standalone ./
COPY --from=build /src/apps/storefront/.next/static ./apps/storefront/.next/static
USER node
EXPOSE 3000
CMD ["node", "apps/storefront/server.js"]
