import type { NextConfig } from "next";

/**
 * Next.js config — sets up API proxy to avoid CORS issues during development.
 * All requests to /api/* from the frontend get forwarded to the Spring Boot
 * backend running on port 8080. In production, this would be handled
 * by a reverse proxy like Nginx instead.
 */
const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: "http://localhost:8080/api/:path*",
      },
    ];
  },
};

export default nextConfig;
