import { ADMIN_EMAIL, ADMIN_PASSWORD, API } from "./specs/helpers";

/**
 * The specs write data, so they must never reach a real store. The test admin account exists only in
 * the throwaway database, so if it can not sign in, stop before any spec runs.
 */
export default async function globalSetup() {
  let status: number | string;
  try {
    const response = await fetch(`${API}/auth/admin/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: ADMIN_EMAIL, password: ADMIN_PASSWORD }),
    });
    status = response.status;
  } catch {
    status = "no response";
  }
  if (status !== 200) {
    throw new Error(
      `Refusing to run: the test admin could not sign in at ${API} (${status}). ` +
        "That API is not using the throwaway test database. See tests/e2e/README.md.",
    );
  }
}
