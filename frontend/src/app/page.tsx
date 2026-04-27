"use client";

import Link from "next/link";

/**
 * Landing page — the first thing users see.
 * Designed to look premium with a gradient hero section,
 * feature cards with hover effects, and clear CTAs.
 */
export default function LandingPage() {
  return (
    <div className="min-h-screen flex flex-col">
      {/* ==================== Navbar ==================== */}
      <nav className="flex items-center justify-between px-6 py-4 border-b border-border-subtle">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-accent flex items-center justify-center">
            <span className="text-white font-bold text-sm">AI</span>
          </div>
          <span className="text-lg font-semibold tracking-tight">
            InterviewAI
          </span>
        </div>

        <div className="flex items-center gap-3">
          <Link
            href="/login"
            className="px-4 py-2 text-sm text-text-secondary hover:text-foreground transition-colors"
          >
            Sign In
          </Link>
          <Link
            href="/register"
            className="px-4 py-2 text-sm rounded-lg bg-accent hover:bg-accent-hover text-white font-medium transition-all duration-200 hover:shadow-lg hover:shadow-accent/20"
          >
            Get Started
          </Link>
        </div>
      </nav>

      {/* ==================== Hero Section ==================== */}
      <main className="flex-1 flex flex-col items-center justify-center px-6 py-20">
        {/* Badge */}
        <div className="animate-fade-in mb-6 px-4 py-1.5 rounded-full border border-accent/30 bg-accent-subtle text-accent text-sm font-medium">
          ✨ Powered by Google Gemini AI
        </div>

        {/* Headline */}
        <h1 className="animate-fade-in text-5xl md:text-6xl lg:text-7xl font-bold text-center max-w-4xl leading-tight tracking-tight">
          Ace Your Next
          <span className="bg-clip-text text-transparent bg-gradient-to-r from-accent to-cyan-400">
            {" "}
            Interview
          </span>
        </h1>

        {/* Subheadline */}
        <p className="animate-slide-up mt-6 text-lg md:text-xl text-text-secondary text-center max-w-2xl leading-relaxed">
          Upload your resume, get AI-generated questions tailored to your
          skills, and receive real-time feedback with scoring — all powered by
          Gemini AI.
        </p>

        {/* CTA buttons */}
        <div className="animate-slide-up mt-10 flex gap-4">
          <Link
            href="/register"
            className="px-8 py-3.5 rounded-xl bg-accent hover:bg-accent-hover text-white font-semibold text-base transition-all duration-300 hover:shadow-xl hover:shadow-accent/25 hover:-translate-y-0.5"
          >
            Start Practicing Free →
          </Link>
          <Link
            href="/login"
            className="px-8 py-3.5 rounded-xl border border-border hover:border-text-muted text-text-secondary hover:text-foreground font-medium text-base transition-all duration-200"
          >
            Sign In
          </Link>
        </div>

        {/* ==================== Feature Cards ==================== */}
        <div className="mt-24 w-full max-w-5xl grid grid-cols-1 md:grid-cols-3 gap-5">
          <FeatureCard
            icon="📄"
            title="Smart Resume Analysis"
            description="Upload your PDF resume and get an AI-powered breakdown of your skills, strengths, and gaps in seconds."
          />
          <FeatureCard
            icon="🎯"
            title="Personalized Questions"
            description="Receive interview questions specifically crafted for your experience level and target role."
          />
          <FeatureCard
            icon="📊"
            title="Real-time Scoring"
            description="Get instant feedback with relevance, depth, and clarity scores plus an ideal answer for comparison."
          />
        </div>
      </main>

      {/* ==================== Footer ==================== */}
      <footer className="border-t border-border-subtle px-6 py-6 text-center text-text-muted text-sm">
        Built with Spring Boot, Next.js & Gemini AI — by{" "}
        <a
          href="https://github.com/ShivamChauhan-108"
          target="_blank"
          rel="noopener noreferrer"
          className="text-accent hover:underline"
        >
          Shivam Chauhan
        </a>
      </footer>
    </div>
  );
}

/**
 * Reusable feature card component with a hover glow effect.
 * Uses CSS transitions for the border color and shadow on hover.
 */
function FeatureCard({
  icon,
  title,
  description,
}: {
  icon: string;
  title: string;
  description: string;
}) {
  return (
    <div className="group p-6 rounded-2xl border border-border-subtle bg-surface hover:border-accent/40 transition-all duration-300 hover:shadow-lg hover:shadow-accent/5">
      <div className="text-3xl mb-4">{icon}</div>
      <h3 className="text-lg font-semibold mb-2 group-hover:text-accent transition-colors">
        {title}
      </h3>
      <p className="text-text-secondary text-sm leading-relaxed">
        {description}
      </p>
    </div>
  );
}
