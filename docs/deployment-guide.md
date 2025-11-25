# Deployment guide

The plan from the architecture brief:

| App | Host | Example domain |
|---|---|---|
| Storefront | Vercel | store.example.com |
| Admin | Vercel | admin.example.com |
| API | Google Cloud Run | api.example.com |
| Database | Cloud SQL for MySQL | private connection |
| Photos | Google Cloud Storage | |

The workflows and Dockerfiles in this repo have not been run against real cloud accounts yet, so expect to adjust them on first use.

## API environment

| Variable | Notes |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | Use a dedicated database user, not `root` |
| `JWT_SECRET` | Random, at least 32 characters. The API refuses to start with a shorter one. Changing it signs everyone out |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Creates the first admin at startup if that account does not exist. Change the password afterwards |
| `CORS_ALLOWED_ORIGINS` | The storefront and admin URLs, comma separated |
| `STORAGE_PROVIDER` | `gcs` in production |
| `STORAGE_BUCKET` | Bucket for photos. Its objects must be publicly readable |

Cloud Run's disk does not persist, so `STORAGE_PROVIDER=local` must not be used there or uploaded photos will vanish.

## Frontend environment

| App | Variable | Notes |
|---|---|---|
| Both | `NEXT_PUBLIC_API_URL` | Public API URL. Set at build time |
| Storefront | `API_URL` | API URL used by the server. Can be the same one |

## GitHub Actions

Each workflow runs only when its own app changes. Deploy jobs stay off until configured:

- **API:** repository variables `GCP_PROJECT` and `GCP_REGION`, and secret `GCP_CREDENTIALS`.
- **Frontends:** repository variable `VERCEL_ENABLED=true`, and secrets `VERCEL_TOKEN`, `VERCEL_ORG_ID`, `VERCEL_STOREFRONT_PROJECT_ID` and `VERCEL_ADMIN_PROJECT_ID`.

Also protect `main` so changes go through pull requests.

## Before going live

- Replace the placeholder business details in `apps/storefront/src/lib/site.ts`.
- Check the policy wording in `apps/storefront/src/components/product/TrustGrid.tsx` against your real delivery, payment and returns terms.
- Upload real photos, and replace the demo products. The demo data only loads in the `dev` profile.
- Add rate limiting in front of the login, contact and newsletter endpoints, for example at your load balancer. The API does not limit request rates itself.
