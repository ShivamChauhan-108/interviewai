import type { NextConfig } from "next";

/**
 * Next.js config — sets up API proxy to avoid CORS issues.
 * All requests to /api/* from the frontend get forwarded to the Spring Boot
 * backend via a server-side rewrite (no CORS needed).
 *
 * In development this hits localhost:8080.
 * In production, set the BACKEND_URL environment variable on Vercel to your
 * deployed backend URL (e.g. https://interviewai-production-d869.up.railway.app).
 *
 * NOTE: BACKEND_URL intentionally does NOT have the NEXT_PUBLIC_ prefix,
 * so it is only available server-side (in rewrites) and never exposed to the browser.
 */
const nextConfig: NextConfig = {
  async rewrites() {
    const backendUrl = process.env.BACKEND_URL || "http://localhost:8080";
    return [
      {
        source: "/api/:path*",
        destination: `${backendUrl}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;

