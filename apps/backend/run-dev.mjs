// Starts the API with the variables from apps/backend/.env. Works in any shell on any OS.
import { spawn } from "node:child_process";
import { existsSync, readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
const envFile = join(here, ".env");
if (!existsSync(envFile)) {
  console.error("Missing apps/backend/.env. Copy it from .env.example first.");
  process.exit(1);
}

const env = { ...process.env };
for (const line of readFileSync(envFile, "utf8").split(/\r?\n/)) {
  const match = line.match(/^\s*([A-Z_][A-Z0-9_]*)\s*=\s*(.*)$/);
  if (match && !(match[1] in process.env)) {
    env[match[1]] = match[2].replace(/^(['"])(.*)\1$/, "$2");
  }
}

const child = spawn("mvn", ["-B", "spring-boot:run"], { cwd: here, env, stdio: "inherit", shell: process.platform === "win32" });
child.on("exit", (code) => process.exit(code ?? 1));
