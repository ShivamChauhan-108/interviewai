/**
 * TypeScript interfaces that mirror the backend DTOs.
 * Keeping these in sync with the Spring Boot response classes
 * makes it easier to catch mismatches at compile time.
 */

// ---- Auth ----
export interface AuthResponse {
  token: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

// ---- Resume Analysis ----
export interface ResumeAnalysisResponse {
  skills: string[];
  experienceLevel: string;
  suggestedRole: string;
  strengths: string[];
  gaps: string[];
  recommendations: string[];
  overallSummary: string;
}

// ---- Interview ----
export interface InterviewStartRequest {
  roleTarget: string;
  difficulty: "EASY" | "MEDIUM" | "HARD";
  numberOfQuestions: number;
}

export interface QuestionResponse {
  questionId: number;
  questionText: string;
  questionType: string;
  questionNumber: number;
  totalQuestions: number;
}

export interface AnswerRequest {
  questionId: number;
  answer: string;
}

export interface FeedbackResponse {
  questionId: number;
  questionText: string;
  userAnswer: string;
  scoreRelevance: number;
  scoreDepth: number;
  scoreClarity: number;
  overallScore: number;
  feedback: string;
  idealAnswer: string;
  improvementTips: string;
}

// ---- Dashboard ----
export interface DashboardStats {
  totalSessions: number;
  averageScore: number;
  totalQuestionsAnswered: number;
  topSkills: string[];
}

// ---- User Profile ----
export interface UserProfile {
  fullName: string;
  email: string;
  resumePath: string | null;
  skills: string[];
}

// ---- Session History ----
export interface SessionSummary {
  sessionId: number;
  roleTarget: string;
  difficulty: string;
  averageScore: number;
  totalQuestions: number;
  createdAt: string;
}

// ---- User state for auth context ----
export interface User {
  email: string;
  fullName: string;
  token: string;
}
