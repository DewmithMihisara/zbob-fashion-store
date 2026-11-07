# Build from the repository root:
#   docker build -f infrastructure/docker/admin.Dockerfile \
#     --build-arg NEXT_PUBLIC_API_URL=https://api.example.com/api/v1 -t zbob-admin .
FROM node:24-slim AS build
RUN corepack enable pnpm
WORKDIR /src
COPY pnpm-lock.yaml pnpm-workspace.yaml package.json ./
COPY packages ./packages
COPY apps/admin ./apps/admin
RUN pnpm install --frozen-lockfile --filter @zbob/admin...
ARG NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
RUN pnpm --filter @zbob/admin build

FROM node:24-slim
WORKDIR /app
ENV NODE_ENV=production PORT=3001 HOSTNAME=0.0.0.0
COPY --from=build /src/apps/admin/.next/standalone ./
COPY --from=build /src/apps/admin/.next/static ./apps/admin/.next/static
USER node
EXPOSE 3001
CMD ["node", "apps/admin/server.js"]
