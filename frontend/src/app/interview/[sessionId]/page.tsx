"use client";

import { useEffect, useState, useRef } from "react";
import { useRouter, useParams } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { getNextQuestion, submitAnswer, endInterview } from "@/lib/api";
import type { QuestionResponse, FeedbackResponse } from "@/lib/types";

export default function InterviewPage() {
  const { user, isLoading: isAuthLoading } = useAuth();
  const router = useRouter();
  const params = useParams();
  const sessionId = parseInt(params.sessionId as string, 10);

  const [question, setQuestion] = useState<QuestionResponse | null>(null);
  const [answer, setAnswer] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [feedback, setFeedback] = useState<FeedbackResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // Auto-resize textarea
  const handleInput = () => {
    if (textareaRef.current) {
      textareaRef.current.style.height = "auto";
      textareaRef.current.style.height = \`\${textareaRef.current.scrollHeight}px\`;
    }
  };

  useEffect(() => {
    if (!isAuthLoading && !user) {
      router.push("/login");
    } else if (user && sessionId) {
      loadNextQuestion();
    }
  }, [user, isAuthLoading, sessionId]);

  const loadNextQuestion = async () => {
    try {
      setIsLoading(true);
      setError("");
      setFeedback(null);
      setAnswer("");
      
      const q = await getNextQuestion(sessionId);
      setQuestion(q);
      
      // Reset textarea height
      if (textareaRef.current) {
        textareaRef.current.style.height = "auto";
      }
    } catch (err) {
      const msg = err instanceof Error ? err.message : "Failed to load question";
      if (msg.includes("All questions answered") || msg.includes("already completed")) {
        // Automatically end the session and redirect to summary
        handleEndSession();
      } else {
        setError(msg);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async () => {
    if (!answer.trim() || !question) return;

    try {
      setIsSubmitting(true);
      setError("");
      
      // Send the answer to the backend
      const fb = await submitAnswer(question.questionId, answer);
      setFeedback(fb);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to submit answer");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEndSession = async () => {
    try {
      await endInterview(sessionId);
      router.push("/dashboard");
    } catch (err) {
      console.error("Failed to end session cleanly", err);
      router.push("/dashboard"); // Force redirect anyway
    }
  };

  if (isAuthLoading || isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-pulse flex flex-col items-center">
          <div className="w-12 h-12 border-4 border-accent border-t-transparent rounded-full animate-spin mb-4"></div>
          <p className="text-text-secondary">Loading AI Interview Environment...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col">
      {/* Header */}
      <header className="px-6 py-4 border-b border-border-subtle bg-surface/80 backdrop-blur-md sticky top-0 z-10 flex justify-between items-center">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-accent/20 border border-accent/30 flex items-center justify-center text-accent">
            🎙️
          </div>
          <div>
            <h1 className="font-semibold leading-tight">Live Interview</h1>
            {question && (
              <p className="text-xs text-text-muted">
                Question {question.questionNumber} of {question.totalQuestions} • {question.questionType}
              </p>
            )}
          </div>
        </div>
        
        <button
          onClick={handleEndSession}
          className="px-4 py-2 rounded-lg text-sm font-medium text-error hover:bg-error-subtle transition-colors"
        >
          End Session
        </button>
      </header>

      <main className="flex-1 max-w-5xl mx-auto w-full p-6 grid grid-cols-1 lg:grid-cols-2 gap-8">
        
        {/* Left Column: Question & Answer Input */}
        <div className="flex flex-col space-y-6">
          {/* Question Card */}
          <div className="p-6 rounded-2xl bg-surface border border-border-subtle shadow-sm animate-slide-up">
            <h2 className="text-sm font-medium text-accent mb-3 uppercase tracking-wider">
              {question?.questionType?.replace("_", " ")} QUESTION
            </h2>
            <p className="text-lg leading-relaxed text-foreground font-medium">
              {question?.questionText}
            </p>
          </div>

          {error && (
            <div className="p-4 rounded-xl bg-error-subtle border border-error/20 text-error text-sm">
              {error}
            </div>
          )}

          {/* Answer Textarea */}
          <div className="flex-1 flex flex-col animate-slide-up" style={{ animationDelay: "100ms" }}>
            <label className="text-sm font-medium text-text-secondary mb-2">
              Your Answer
            </label>
            <div className="relative flex-1 min-h-[200px]">
              <textarea
                ref={textareaRef}
                value={answer}
                onChange={(e) => {
                  setAnswer(e.target.value);
                  handleInput();
                }}
                disabled={isSubmitting || !!feedback}
                placeholder="Type your answer here as if you are speaking to an interviewer..."
                className="w-full h-full min-h-[200px] p-4 rounded-2xl bg-background border border-border focus:border-accent focus:ring-1 focus:ring-accent outline-none resize-none transition-all disabled:opacity-60 disabled:cursor-not-allowed"
              />
              
              {/* Submit Button inside the textarea box at the bottom right */}
              {!feedback && (
                <div className="absolute bottom-4 right-4">
                  <button
                    onClick={handleSubmit}
                    disabled={isSubmitting || answer.trim().length < 10}
                    className="px-6 py-2.5 rounded-xl bg-accent hover:bg-accent-hover disabled:bg-surface-hover disabled:text-text-muted text-white font-medium shadow-md transition-all flex items-center gap-2"
                  >
                    {isSubmitting ? (
                      <>
                        <div className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin"></div>
                        Evaluating...
                      </>
                    ) : (
                      "Submit Answer"
                    )}
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right Column: AI Feedback (Only shows after submitting) */}
        <div className="flex flex-col">
          {feedback ? (
            <div className="animate-slide-up h-full flex flex-col">
              <div className="p-6 rounded-2xl bg-surface border border-accent/30 shadow-glow flex-1 overflow-y-auto custom-scrollbar">
                <div className="flex items-center gap-3 mb-6">
                  <div className="w-10 h-10 rounded-full bg-accent/20 flex items-center justify-center text-xl">
                    🤖
                  </div>
                  <div>
                    <h3 className="font-semibold text-lg text-foreground">AI Evaluation</h3>
                    <p className="text-sm text-text-secondary">Real-time feedback based on your response</p>
                  </div>
                </div>

                {/* Score Pills */}
                <div className="flex flex-wrap gap-3 mb-8">
                  <ScorePill label="Overall" score={feedback.overallScore} isMain />
                  <ScorePill label="Relevance" score={feedback.scoreRelevance} />
                  <ScorePill label="Depth" score={feedback.scoreDepth} />
                  <ScorePill label="Clarity" score={feedback.scoreClarity} />
                </div>

                {/* Feedback Content */}
                <div className="space-y-6">
                  <div>
                    <h4 className="text-sm font-semibold text-accent uppercase tracking-wider mb-2">Feedback</h4>
                    <p className="text-text-secondary leading-relaxed whitespace-pre-line">
                      {feedback.feedback}
                    </p>
                  </div>

                  {feedback.improvementTips && (
                    <div className="p-4 rounded-xl bg-warning-subtle border border-warning/20">
                      <h4 className="text-sm font-semibold text-warning uppercase tracking-wider mb-2">How to Improve</h4>
                      <p className="text-warning/90 leading-relaxed text-sm">
                        {feedback.improvementTips}
                      </p>
                    </div>
                  )}

                  <div className="p-4 rounded-xl bg-background border border-border">
                    <h4 className="text-sm font-semibold text-text-muted uppercase tracking-wider mb-2">Ideal Answer Approach</h4>
                    <p className="text-text-secondary leading-relaxed text-sm whitespace-pre-line">
                      {feedback.idealAnswer}
                    </p>
                  </div>
                </div>
              </div>

              {/* Next Question Button */}
              <div className="mt-6">
                <button
                  onClick={question?.questionNumber === question?.totalQuestions ? handleEndSession : loadNextQuestion}
                  className="w-full py-4 rounded-xl bg-foreground text-background hover:bg-text-secondary font-bold text-lg transition-all shadow-lg"
                >
                  {question?.questionNumber === question?.totalQuestions 
                    ? "Complete Interview & See Results 🎉" 
                    : "Next Question ➡️"}
                </button>
              </div>
            </div>
          ) : (
            /* Placeholder before answering */
            <div className="h-full rounded-2xl border border-dashed border-border flex flex-col items-center justify-center p-10 text-center bg-surface/30">
              <div className="w-16 h-16 rounded-2xl bg-surface border border-border flex items-center justify-center text-3xl mb-4 opacity-50">
                🤖
              </div>
              <h3 className="text-lg font-medium text-text-secondary mb-2">Awaiting Your Answer</h3>
              <p className="text-sm text-text-muted max-w-xs">
                Submit your response on the left to receive instant, AI-powered feedback, scoring, and an ideal reference answer.
              </p>
            </div>
          )}
        </div>

      </main>
    </div>
  );
}

// Helper to render score pills
function ScorePill({ label, score, isMain = false }: { label: string; score: number; isMain?: boolean }) {
  // Determine color based on score
  let colorClass = "bg-surface-hover text-text-secondary border-border";
  
  if (score >= 8) {
    colorClass = "bg-success-subtle text-success border-success/30";
  } else if (score >= 5) {
    colorClass = "bg-warning-subtle text-warning border-warning/30";
  } else if (score > 0) {
    colorClass = "bg-error-subtle text-error border-error/30";
  }

  if (isMain) {
    return (
      <div className={\`flex items-center gap-2 px-4 py-2 rounded-full border \${colorClass}\`}>
        <span className="text-sm font-medium">{label}:</span>
        <span className="font-bold text-lg">{score.toFixed(1)}<span className="text-xs opacity-70">/10</span></span>
      </div>
    );
  }

  return (
    <div className={\`flex items-center gap-1.5 px-3 py-1.5 rounded-full border \${colorClass}\`}>
      <span className="text-xs font-medium opacity-80">{label}:</span>
      <span className="font-bold text-sm">{score}<span className="text-[10px] opacity-70">/10</span></span>
    </div>
  );
}
