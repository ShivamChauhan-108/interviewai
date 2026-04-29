"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { getInterviewHistory } from "@/lib/api";
import type { SessionSummary } from "@/lib/types";

/**
 * Session History page — shows a list of all completed interview sessions.
 * Each card shows the role, difficulty, score, and date.
 * Clicking a card navigates to the detailed session review.
 */
export default function HistoryPage() {
  const { user, isLoading: isAuthLoading } = useAuth();
  const router = useRouter();
  const [sessions, setSessions] = useState<SessionSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!isAuthLoading && !user) {
      router.push("/login");
    } else if (user) {
      loadHistory();
    }
  }, [user, isAuthLoading]);

  const loadHistory = async () => {
    try {
      const data = await getInterviewHistory();
      setSessions(data);
    } catch (err) {
      console.error("Failed to load history", err);
    } finally {
      setIsLoading(false);
    }
  };

  // Format the date string into something readable
  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return "In Progress";
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-IN", {
      day: "numeric",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  // Color coding based on score
  const getScoreColor = (score: number | null) => {
    if (!score) return "text-text-muted";
    if (score >= 7) return "text-success";
    if (score >= 5) return "text-warning";
    return "text-error";
  };

  const getDifficultyBadge = (difficulty: string) => {
    const colors: Record<string, string> = {
      EASY: "bg-success-subtle text-success border-success/20",
      MEDIUM: "bg-warning-subtle text-warning border-warning/20",
      HARD: "bg-error-subtle text-error border-error/20",
    };
    return colors[difficulty] || "bg-surface text-text-secondary border-border";
  };

  if (isAuthLoading || isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-pulse flex flex-col items-center">
          <div className="w-12 h-12 border-4 border-accent border-t-transparent rounded-full animate-spin mb-4"></div>
          <p className="text-text-secondary">Loading session history...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      {/* Navbar */}
      <nav className="flex items-center justify-between px-8 py-4 border-b border-border-subtle bg-surface/50 backdrop-blur-md sticky top-0 z-10">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-accent flex items-center justify-center">
            <span className="text-white font-bold text-sm">AI</span>
          </div>
          <span className="text-lg font-semibold tracking-tight">
            Session History
          </span>
        </div>

        <Link
          href="/dashboard"
          className="px-4 py-2 text-sm rounded-lg border border-border hover:bg-surface-hover transition-colors"
        >
          ← Back to Dashboard
        </Link>
      </nav>

      <main className="max-w-4xl mx-auto px-6 py-10">
        <h1 className="text-3xl font-bold mb-2">Past Interviews</h1>
        <p className="text-text-secondary mb-8">
          Review your performance across all sessions
        </p>

        {sessions.length === 0 ? (
          <div className="text-center py-20 border border-dashed border-border rounded-2xl bg-surface/30">
            <div className="text-5xl mb-4 opacity-50">📋</div>
            <h3 className="text-lg font-medium text-text-secondary mb-2">
              No sessions yet
            </h3>
            <p className="text-sm text-text-muted mb-6">
              Start your first mock interview to see results here.
            </p>
            <Link
              href="/dashboard"
              className="px-6 py-2.5 rounded-lg bg-accent hover:bg-accent-hover text-white font-medium text-sm transition-colors"
            >
              Go to Dashboard
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            {sessions.map((session, index) => (
              <Link
                key={session.sessionId}
                href={`/history/${session.sessionId}`}
                className="block group animate-fade-in"
                style={{ animationDelay: `${index * 50}ms` }}
              >
                <div className="p-6 rounded-2xl border border-border-subtle bg-surface hover:border-accent/30 transition-all duration-300 hover:shadow-lg hover:shadow-accent/5">
                  <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                    {/* Left: Role & Meta */}
                    <div className="flex-1">
                      <h3 className="text-lg font-semibold group-hover:text-accent transition-colors">
                        {session.roleTarget}
                      </h3>
                      <div className="flex items-center gap-3 mt-2">
                        <span
                          className={`px-2.5 py-0.5 rounded-full text-xs font-medium border ${getDifficultyBadge(session.difficulty)}`}
                        >
                          {session.difficulty}
                        </span>
                        <span className="text-xs text-text-muted">
                          {session.totalQuestions} questions
                        </span>
                        <span className="text-xs text-text-muted">
                          {formatDate(session.startedAt)}
                        </span>
                      </div>
                    </div>

                    {/* Right: Score */}
                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <p className="text-xs text-text-muted uppercase tracking-wider">
                          Avg Score
                        </p>
                        <p
                          className={`text-2xl font-bold ${getScoreColor(session.avgScore)}`}
                        >
                          {session.avgScore
                            ? `${Number(session.avgScore).toFixed(1)}`
                            : "—"}
                          <span className="text-sm font-normal opacity-60">
                            /10
                          </span>
                        </p>
                      </div>

                      {/* Status indicator */}
                      <div
                        className={`w-3 h-3 rounded-full ${
                          session.status === "COMPLETED"
                            ? "bg-success"
                            : "bg-warning animate-pulse"
                        }`}
                        title={session.status}
                      />

                      {/* Arrow */}
                      <span className="text-text-muted group-hover:text-accent transition-colors text-lg">
                        →
                      </span>
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
