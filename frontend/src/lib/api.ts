/**
 * Centralized API client for communicating with the Spring Boot backend.
 *
 * Every request goes through the `apiFetch` wrapper which:
 *  - prepends the backend base URL
 *  - attaches the JWT token from localStorage (if available)
 *  - sets Content-Type to JSON for non-FormData requests
 *  - throws a descriptive error on non-2xx responses
 *
 * This pattern avoids duplicating fetch + headers logic in every component.
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

/**
 * Core fetch wrapper that handles auth headers and error parsing.
 * Components should use the specific helper functions below instead
 * of calling this directly — keeps things cleaner.
 */
async function apiFetch(
  endpoint: string,
  options: RequestInit = {}
): Promise<Response> {
  const token =
    typeof window !== "undefined" ? localStorage.getItem("token") : null;

  const headers: Record<string, string> = {};

  // Only set Content-Type for JSON payloads (FormData sets its own boundary)
  if (!(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }

  // Attach JWT if the user is logged in
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      ...headers,
      ...options.headers,
    },
  });

  // If the backend returns an error, try to parse the message from JSON
  if (!response.ok) {
    const errorBody = await response.json().catch(() => null);
    const message =
      errorBody?.message || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return response;
}

// ===================== Auth Endpoints =====================

export async function registerUser(
  fullName: string,
  email: string,
  password: string
) {
  const res = await apiFetch("/api/auth/register", {
    method: "POST",
    body: JSON.stringify({ fullName, email, password }),
  });
  return res.json();
}

export async function loginUser(email: string, password: string) {
  const res = await apiFetch("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
  return res.json();
}

// ===================== Resume Endpoints =====================

export async function uploadResume(file: File) {
  const formData = new FormData();
  formData.append("file", file);

  const res = await apiFetch("/api/resume/upload", {
    method: "POST",
    body: formData,
  });
  return res.json();
}

// ===================== Interview Endpoints =====================

export async function startInterview(
  roleTarget: string,
  difficulty: string,
  numberOfQuestions: number
) {
  const res = await apiFetch("/api/interview/start", {
    method: "POST",
    body: JSON.stringify({ roleTarget, difficulty, numberOfQuestions }),
  });
  return res.json();
}

export async function submitAnswer(questionId: number, answer: string) {
  const res = await apiFetch("/api/interview/answer", {
    method: "POST",
    body: JSON.stringify({ questionId, answer }),
  });
  return res.json();
}

// ===================== Dashboard & Profile =====================

export async function getDashboardStats() {
  const res = await apiFetch("/api/dashboard/stats");
  return res.json();
}

export async function getUserProfile() {
  const res = await apiFetch("/api/profile");
  return res.json();
}

export async function getSessionHistory() {
  const res = await apiFetch("/api/dashboard/sessions");
  return res.json();
}

// ===================== Health Check =====================

export async function healthCheck() {
  const res = await apiFetch("/api/health");
  return res.json();
}
