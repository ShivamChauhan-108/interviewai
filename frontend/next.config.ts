import type { NextConfig } from "next";

/**
 * Next.js config — sets up API proxy to avoid CORS issues during development.
 * All requests to /api/* from the frontend get forwarded to the Spring Boot
 * backend. In development this hits localhost:8080. In production, set the
 * NEXT_PUBLIC_API_URL environment variable to your deployed backend URL.
 */
const nextConfig: NextConfig = {
  async rewrites() {
    const backendUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
    return [
      {
        source: "/api/:path*",
        destination: `${backendUrl}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
