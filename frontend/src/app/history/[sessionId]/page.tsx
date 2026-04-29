"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { getSessionDetail } from "@/lib/api";
import type { SessionSummary, QuestionSummary } from "@/lib/types";

/**
 * Session Detail page — shows the full breakdown of a completed interview.
 * Displays overall stats, strong/weak areas, and each question with
 * the user's answer, AI feedback, ideal answer, and individual scores.
 */
export default function SessionDetailPage() {
  const { user, isLoading: isAuthLoading } = useAuth();
  const router = useRouter();
  const params = useParams();
  const sessionId = parseInt(params.sessionId as string, 10);

  const [session, setSession] = useState<SessionSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [expandedQuestion, setExpandedQuestion] = useState<number | null>(null);

  useEffect(() => {
    if (!isAuthLoading && !user) {
      router.push("/login");
    } else if (user && sessionId) {
      loadSession();
    }
  }, [user, isAuthLoading, sessionId]);

  const loadSession = async () => {
    try {
      const data = await getSessionDetail(sessionId);
      setSession(data);
    } catch (err) {
      console.error("Failed to load session detail", err);
    } finally {
      setIsLoading(false);
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 7) return "text-success";
    if (score >= 5) return "text-warning";
    return "text-error";
  };

  const getScoreBg = (score: number) => {
    if (score >= 7) return "bg-success-subtle border-success/20";
    if (score >= 5) return "bg-warning-subtle border-warning/20";
    return "bg-error-subtle border-error/20";
  };

  if (isAuthLoading || isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-pulse flex flex-col items-center">
          <div className="w-12 h-12 border-4 border-accent border-t-transparent rounded-full animate-spin mb-4"></div>
          <p className="text-text-secondary">Loading session details...</p>
        </div>
      </div>
    );
  }

  if (!session) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-text-secondary">Session not found</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen pb-20">
      {/* Navbar */}
      <nav className="flex items-center justify-between px-8 py-4 border-b border-border-subtle bg-surface/50 backdrop-blur-md sticky top-0 z-10">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-accent flex items-center justify-center">
            <span className="text-white font-bold text-sm">AI</span>
          </div>
          <span className="text-lg font-semibold tracking-tight">
            Session Review
          </span>
        </div>

        <Link
          href="/history"
          className="px-4 py-2 text-sm rounded-lg border border-border hover:bg-surface-hover transition-colors"
        >
          ← All Sessions
        </Link>
      </nav>

      <main className="max-w-5xl mx-auto px-6 py-10 space-y-8">
        {/* Header */}
        <div className="animate-fade-in">
          <h1 className="text-3xl font-bold mb-1">{session.roleTarget}</h1>
          <p className="text-text-secondary">
            {session.difficulty} • {session.totalQuestions} questions •{" "}
            {session.status === "COMPLETED" ? "Completed" : "In Progress"}
          </p>
        </div>

        {/* Score Overview */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5 animate-slide-up">
          {/* Main Score */}
          <div className="p-8 rounded-2xl border border-accent/30 bg-surface text-center shadow-glow">
            <p className="text-sm text-text-secondary uppercase tracking-wider mb-2">
              Overall Score
            </p>
            <p className={`text-5xl font-bold ${getScoreColor(Number(session.avgScore))}`}>
              {session.avgScore ? Number(session.avgScore).toFixed(1) : "—"}
              <span className="text-lg font-normal opacity-50">/10</span>
            </p>
          </div>

          {/* Strong Areas */}
          <div className="p-6 rounded-2xl border border-success/20 bg-success-subtle">
            <h3 className="text-sm font-semibold text-success uppercase tracking-wider mb-3">
              💪 Strong Areas
            </h3>
            {session.strongAreas && session.strongAreas.length > 0 ? (
              <ul className="space-y-1.5">
                {session.strongAreas.map((area, i) => (
                  <li key={i} className="text-sm text-success/80 leading-snug">
                    • {area}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-success/60">
                Complete more questions to see strengths
              </p>
            )}
          </div>

          {/* Weak Areas */}
          <div className="p-6 rounded-2xl border border-error/20 bg-error-subtle">
            <h3 className="text-sm font-semibold text-error uppercase tracking-wider mb-3">
              📈 Needs Improvement
            </h3>
            {session.weakAreas && session.weakAreas.length > 0 ? (
              <ul className="space-y-1.5">
                {session.weakAreas.map((area, i) => (
                  <li key={i} className="text-sm text-error/80 leading-snug">
                    • {area}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-error/60">
                No weak areas detected — great job!
              </p>
            )}
          </div>
        </div>

        {/* Overall Feedback */}
        {session.overallFeedback && (
          <div className="p-6 rounded-2xl border border-border-subtle bg-surface animate-slide-up">
            <h3 className="text-sm font-semibold text-accent uppercase tracking-wider mb-3">
              AI Summary
            </h3>
            <p className="text-text-secondary leading-relaxed">
              {session.overallFeedback}
            </p>
          </div>
        )}

        {/* Per-Question Breakdown */}
        <div className="space-y-4">
          <h2 className="text-xl font-bold">Question Breakdown</h2>

          {session.questions && session.questions.length > 0 ? (
            session.questions.map((q, index) => (
              <QuestionCard
                key={q.questionId}
                question={q}
                index={index}
                isExpanded={expandedQuestion === q.questionId}
                onToggle={() =>
                  setExpandedQuestion(
                    expandedQuestion === q.questionId ? null : q.questionId
                  )
                }
                getScoreColor={getScoreColor}
                getScoreBg={getScoreBg}
              />
            ))
          ) : (
            <p className="text-text-muted text-sm">
              No question details available for this session.
            </p>
          )}
        </div>

        {/* Back to Dashboard */}
        <div className="text-center pt-8">
          <Link
            href="/dashboard"
            className="px-8 py-3 rounded-xl bg-accent hover:bg-accent-hover text-white font-semibold transition-all hover:shadow-lg hover:shadow-accent/20"
          >
            Start Another Interview
          </Link>
        </div>
      </main>
    </div>
  );
}

/**
 * Expandable question card — click to toggle the full feedback view.
 * Shows scores in collapsed state, full answer + feedback when expanded.
 */
function QuestionCard({
  question,
  index,
  isExpanded,
  onToggle,
  getScoreColor,
  getScoreBg,
}: {
  question: QuestionSummary;
  index: number;
  isExpanded: boolean;
  onToggle: () => void;
  getScoreColor: (score: number) => string;
  getScoreBg: (score: number) => string;
}) {
  return (
    <div
      className={`rounded-2xl border bg-surface overflow-hidden transition-all duration-300 animate-fade-in ${
        isExpanded ? "border-accent/30" : "border-border-subtle"
      }`}
      style={{ animationDelay: `${index * 80}ms` }}
    >
      {/* Collapsed Header — always visible */}
      <button
        onClick={onToggle}
        className="w-full p-5 flex items-center justify-between text-left hover:bg-surface-hover/50 transition-colors"
      >
        <div className="flex-1 pr-4">
          <div className="flex items-center gap-2 mb-1">
            <span className="text-xs font-medium text-accent bg-accent-subtle px-2 py-0.5 rounded-full">
              Q{index + 1}
            </span>
            <span className="text-xs text-text-muted uppercase">
              {question.questionType?.replace("_", " ")}
            </span>
          </div>
          <p className="text-sm font-medium text-foreground line-clamp-2">
            {question.questionText}
          </p>
        </div>

        <div className="flex items-center gap-3 shrink-0">
          {question.overallScore != null && (
            <span className={`text-lg font-bold ${getScoreColor(question.overallScore)}`}>
              {question.overallScore.toFixed(1)}
            </span>
          )}
          <span className="text-text-muted text-sm">
            {isExpanded ? "▲" : "▼"}
          </span>
        </div>
      </button>

      {/* Expanded Content */}
      {isExpanded && (
        <div className="px-5 pb-6 space-y-5 border-t border-border-subtle pt-5">
          {/* Score pills */}
          <div className="flex flex-wrap gap-2">
            {["Relevance", "Depth", "Clarity"].map((label) => {
              const key = `score${label}` as keyof QuestionSummary;
              const score = question[key] as number;
              return (
                <span
                  key={label}
                  className={`px-3 py-1 rounded-full text-xs font-medium border ${getScoreBg(score || 0)}`}
                >
                  {label}: {score || 0}/10
                </span>
              );
            })}
          </div>

          {/* Your Answer */}
          <div>
            <h4 className="text-xs font-semibold text-text-muted uppercase tracking-wider mb-2">
              Your Answer
            </h4>
            <p className="text-sm text-text-secondary leading-relaxed p-4 rounded-xl bg-background border border-border whitespace-pre-line">
              {question.userAnswer || "Not answered"}
            </p>
          </div>

          {/* AI Feedback */}
          {question.aiFeedback && (
            <div>
              <h4 className="text-xs font-semibold text-accent uppercase tracking-wider mb-2">
                AI Feedback
              </h4>
              <p className="text-sm text-text-secondary leading-relaxed whitespace-pre-line">
                {question.aiFeedback}
              </p>
            </div>
          )}

          {/* Ideal Answer */}
          {question.idealAnswer && (
            <div className="p-4 rounded-xl bg-accent-subtle border border-accent/20">
              <h4 className="text-xs font-semibold text-accent uppercase tracking-wider mb-2">
                Ideal Answer Approach
              </h4>
              <p className="text-sm text-accent/80 leading-relaxed whitespace-pre-line">
                {question.idealAnswer}
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
