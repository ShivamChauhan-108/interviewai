"use client";

import { useEffect, useState, useRef } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import {
  getUserProfile,
  getDashboardStats,
  uploadResume,
  startInterview,
} from "@/lib/api";
import type { UserProfile, DashboardStats } from "@/lib/types";

export default function DashboardPage() {
  const { user, logout, isLoading: isAuthLoading } = useAuth();
  const router = useRouter();

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Resume upload state
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadStatus, setUploadStatus] = useState<
    "idle" | "success" | "error"
  >("idle");
  const [uploadMessage, setUploadMessage] = useState("");

  // Interview settings state
  const [roleTarget, setRoleTarget] = useState("");
  const [difficulty, setDifficulty] = useState("MEDIUM");
  const [numQuestions, setNumQuestions] = useState(3);
  const [isStartingInterview, setIsStartingInterview] = useState(false);
  const [interviewError, setInterviewError] = useState("");

  // Load dashboard data
  const loadData = async () => {
    try {
      setIsLoading(true);
      const [profileData, statsData] = await Promise.all([
        getUserProfile(),
        getDashboardStats().catch(() => null), // If stats fail (e.g. no sessions yet), don't break the page
      ]);
      setProfile(profileData);
      if (statsData) setStats(statsData);
      
      // Auto-fill role target from suggestions if empty
      if (!roleTarget && profileData?.suggestedRole) {
        setRoleTarget(profileData.suggestedRole.split(" / ")[0] || "Software Engineer");
      }
    } catch (error) {
      console.error("Failed to load dashboard data", error);
      // If we get a 401/403, we should probably log out
    } finally {
      setIsLoading(false);
    }
  };

  // Check auth and load data
  useEffect(() => {
    if (!isAuthLoading) {
      if (!user) {
        router.push("/login");
      } else {
        loadData();
      }
    }
  }, [user, isAuthLoading, router]);

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.type !== "application/pdf") {
      setUploadStatus("error");
      setUploadMessage("Please upload a PDF file.");
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setUploadStatus("error");
      setUploadMessage("File size must be less than 5MB.");
      return;
    }

    setIsUploading(true);
    setUploadStatus("idle");
    setUploadMessage("Parsing resume with AI... This may take a minute.");

    try {
      const result = await uploadResume(file);
      setUploadStatus("success");
      setUploadMessage("Resume analyzed successfully!");
      // Reload profile to get updated skills and resume status
      await loadData();
    } catch (error) {
      setUploadStatus("error");
      setUploadMessage(
        error instanceof Error ? error.message : "Failed to upload resume"
      );
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = ""; // Reset input
    }
  };

  const handleStartInterview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!roleTarget) {
      setInterviewError("Please specify a target role");
      return;
    }

    setIsStartingInterview(true);
    setInterviewError("");

    try {
      const data = await startInterview(roleTarget, difficulty, numQuestions);
      // The backend returns a sessionId or the first question.
      // Redirect to the interview page.
      router.push(`/interview/${data.sessionId || "active"}`);
    } catch (error) {
      setInterviewError(
        error instanceof Error ? error.message : "Failed to start interview"
      );
      setIsStartingInterview(false);
    }
  };

  if (isAuthLoading || isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-pulse flex flex-col items-center">
          <div className="w-12 h-12 border-4 border-accent border-t-transparent rounded-full animate-spin mb-4"></div>
          <p className="text-text-secondary">Loading dashboard...</p>
        </div>
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
            Dashboard
          </span>
        </div>

        <div className="flex items-center gap-4">
          <Link
            href="/history"
            className="text-sm font-medium text-text-secondary hover:text-accent transition-colors hidden md:inline-block"
          >
            Session History
          </Link>
          <span className="text-sm text-text-muted hidden md:inline-block border-l border-border-subtle pl-4">
            {user?.email}
          </span>
          <button
            onClick={logout}
            className="px-4 py-2 text-sm rounded-lg border border-border hover:bg-surface-hover transition-colors ml-2"
          >
            Logout
          </button>
        </div>
      </nav>

      <main className="max-w-6xl mx-auto px-6 pt-10 space-y-8">
        <header>
          <h1 className="text-3xl font-bold mb-2">
            Welcome back, {user?.fullName?.split(" ")[0] || "User"} 👋
          </h1>
          <p className="text-text-secondary">
            Ready to ace your next technical interview?
          </p>
        </header>

        {/* Top Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          <StatCard
            title="Total Sessions"
            value={stats?.totalSessions?.toString() || "0"}
            icon="🎯"
          />
          <StatCard
            title="Questions Answered"
            value={stats?.totalQuestionsAnswered?.toString() || "0"}
            icon="📝"
          />
          <StatCard
            title="Avg. Score"
            value={stats?.averageScore ? `${stats.averageScore.toFixed(1)}/10` : "N/A"}
            icon="⭐️"
            isHighlight={stats && stats.averageScore > 7}
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 pt-4">
          {/* Left Column: Resume & Skills */}
          <div className="space-y-8">
            {/* Resume Upload Section */}
            <section className="p-6 rounded-2xl border border-border-subtle bg-surface">
              <h2 className="text-xl font-semibold mb-4">Resume Setup</h2>
              
              {!profile?.resumePath ? (
                <div className="text-center p-6 border-2 border-dashed border-border rounded-xl hover:border-accent/50 transition-colors bg-background">
                  <p className="text-sm text-text-secondary mb-4">
                    Upload your resume to get personalized questions based on your experience.
                  </p>
                  <input
                    type="file"
                    accept=".pdf"
                    className="hidden"
                    ref={fileInputRef}
                    onChange={handleFileUpload}
                  />
                  <button
                    onClick={() => fileInputRef.current?.click()}
                    disabled={isUploading}
                    className="px-6 py-2.5 rounded-lg bg-accent hover:bg-accent-hover text-white font-medium text-sm transition-colors disabled:opacity-50 flex items-center justify-center w-full gap-2"
                  >
                    {isUploading ? (
                      <>
                        <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                        Analyzing PDF...
                      </>
                    ) : (
                      "Upload PDF Resume"
                    )}
                  </button>
                </div>
              ) : (
                <div className="flex items-center justify-between p-4 rounded-xl bg-success-subtle border border-success/20">
                  <div className="flex items-center gap-3">
                    <div className="text-success text-2xl">✓</div>
                    <div>
                      <p className="font-medium text-success">Resume Active</p>
                      <p className="text-xs text-success/70">AI has analyzed your profile</p>
                    </div>
                  </div>
                  <input
                    type="file"
                    accept=".pdf"
                    className="hidden"
                    ref={fileInputRef}
                    onChange={handleFileUpload}
                  />
                  <button
                    onClick={() => fileInputRef.current?.click()}
                    disabled={isUploading}
                    className="text-xs px-3 py-1.5 bg-success/20 hover:bg-success/30 text-success rounded-md transition-colors"
                  >
                    {isUploading ? "Uploading..." : "Update"}
                  </button>
                </div>
              )}

              {/* Upload Status Messages */}
              {uploadStatus !== "idle" && (
                <div
                  className={`mt-4 p-3 rounded-lg text-sm ${
                    uploadStatus === "success"
                      ? "bg-success-subtle text-success border border-success/20"
                      : "bg-error-subtle text-error border border-error/20"
                  }`}
                >
                  {uploadMessage}
                </div>
              )}
            </section>

            {/* Extracted Skills Section */}
            {profile?.skills && profile.skills.length > 0 && (
              <section className="p-6 rounded-2xl border border-border-subtle bg-surface">
                <h2 className="text-xl font-semibold mb-4">Your Tech Stack</h2>
                <div className="flex flex-wrap gap-2">
                  {profile.skills.map((skill, index) => (
                    <span
                      key={index}
                      className="px-3 py-1 text-xs font-medium bg-background border border-border rounded-full text-text-secondary"
                    >
                      {skill}
                    </span>
                  ))}
                </div>
              </section>
            )}
          </div>

          {/* Right Column: Start Interview */}
          <div className="lg:col-span-2">
            <section className="p-8 rounded-2xl border border-border-subtle bg-gradient-to-b from-surface to-background relative overflow-hidden">
              {/* Decorative background element */}
              <div className="absolute top-0 right-0 -mr-20 -mt-20 w-64 h-64 bg-accent/10 rounded-full blur-3xl pointer-events-none"></div>
              
              <h2 className="text-2xl font-bold mb-2">New Mock Interview</h2>
              <p className="text-text-secondary mb-8">
                Configure your session. The AI will act as the technical interviewer.
              </p>

              {!profile?.resumePath && (
                <div className="mb-6 p-4 rounded-xl border border-warning/20 bg-warning-subtle text-warning text-sm flex gap-3">
                  <span>⚠️</span>
                  <span>
                    You haven't uploaded a resume yet. The interview will be generic. 
                    Upload a resume first for a personalized experience.
                  </span>
                </div>
              )}

              {interviewError && (
                <div className="mb-6 p-3 rounded-lg bg-error-subtle text-error border border-error/20 text-sm">
                  {interviewError}
                </div>
              )}

              <form onSubmit={handleStartInterview} className="space-y-6 relative z-10">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* Role */}
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-foreground">Target Role</label>
                    <input
                      type="text"
                      value={roleTarget}
                      onChange={(e) => setRoleTarget(e.target.value)}
                      placeholder="e.g. Senior Java Developer"
                      required
                      className="w-full px-4 py-3 rounded-xl bg-background border border-border focus:border-accent focus:ring-1 focus:ring-accent outline-none transition-all"
                    />
                  </div>

                  {/* Difficulty */}
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-foreground">Difficulty Level</label>
                    <select
                      value={difficulty}
                      onChange={(e) => setDifficulty(e.target.value)}
                      className="w-full px-4 py-3 rounded-xl bg-background border border-border focus:border-accent focus:ring-1 focus:ring-accent outline-none transition-all appearance-none"
                    >
                      <option value="EASY">Easy (Conceptual)</option>
                      <option value="MEDIUM">Medium (Practical)</option>
                      <option value="HARD">Hard (Deep Dive)</option>
                    </select>
                  </div>
                </div>

                <div className="pt-4">
                  <button
                    type="submit"
                    disabled={isStartingInterview}
                    className="w-full py-4 rounded-xl bg-accent hover:bg-accent-hover text-white font-bold text-lg transition-all duration-300 shadow-lg shadow-accent/25 hover:shadow-accent/40 disabled:opacity-70 flex justify-center items-center gap-2"
                  >
                    {isStartingInterview ? (
                      <>
                        <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                        Generating Interview Context...
                      </>
                    ) : (
                      "Start Interview Session 🚀"
                    )}
                  </button>
                </div>
              </form>
            </section>
          </div>
        </div>
      </main>
    </div>
  );
}

// Helper component for stats
function StatCard({ title, value, icon, isHighlight = false }: { title: string; value: string; icon: string; isHighlight?: boolean }) {
  return (
    <div className={`p-6 rounded-2xl border bg-surface transition-all duration-300 ${isHighlight ? 'border-accent/30 shadow-glow' : 'border-border-subtle'}`}>
      <div className="flex justify-between items-start">
        <h3 className="text-text-secondary font-medium text-sm">{title}</h3>
        <span className="text-xl">{icon}</span>
      </div>
      <p className="text-3xl font-bold mt-4 tracking-tight">{value}</p>
    </div>
  );
}
