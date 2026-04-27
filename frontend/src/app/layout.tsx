import type { Metadata } from "next";
import { Inter } from "next/font/google";
import { AuthProvider } from "@/context/AuthContext";
import "./globals.css";

/**
 * Using Inter font — it's the industry standard for modern SaaS dashboards.
 * Companies like Vercel, Linear, and Notion all use it.
 */
const inter = Inter({
  subsets: ["latin"],
  variable: "--font-geist-sans",
  display: "swap",
});

export const metadata: Metadata = {
  title: "InterviewAI — AI-Powered Interview Preparation",
  description:
    "Upload your resume, get AI-generated interview questions tailored to your skills, and receive real-time feedback on your answers.",
  keywords: [
    "AI interview",
    "interview preparation",
    "resume analysis",
    "mock interview",
  ],
};

/**
 * Root layout wraps the entire application.
 * AuthProvider sits here so every page can access user state via useAuth().
 */
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={`${inter.variable} h-full`}>
      <body className="min-h-full bg-background text-foreground antialiased">
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
