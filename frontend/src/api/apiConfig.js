// VITE_API_URL is set at build time (Vercel project settings, or a local .env file).
// Falls back to localhost:8080 so `npm run dev` keeps working with zero setup.
export const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";