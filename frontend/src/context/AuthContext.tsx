"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  ReactNode,
} from "react";
import { User } from "@/lib/types";

/**
 * Auth context manages the logged-in user state across the entire app.
 *
 * On mount, it checks localStorage for a saved token + user info.
 * When the user logs in, we save the token and user data to localStorage
 * so they stay logged in even after refreshing the page.
 *
 * Any component can call `useAuth()` to access:
 *  - user: the currently logged-in user (or null)
 *  - login(): save credentials after successful auth
 *  - logout(): clear everything and redirect to login
 *  - isLoading: true while we check localStorage on first render
 */

interface AuthContextType {
  user: User | null;
  login: (token: string, email: string, fullName: string) => void;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // On first render, check if the user was previously logged in
  useEffect(() => {
    try {
      const token = localStorage.getItem("token");
      const email = localStorage.getItem("email");
      const fullName = localStorage.getItem("fullName");

      if (token && email && fullName) {
        setUser({ token, email, fullName });
      }
    } catch (error) {
      // localStorage might not be available (SSR edge case)
      console.error("Failed to restore auth state:", error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Called after a successful login or register API response.
   * Stores credentials in localStorage and updates the React state.
   */
  const login = (token: string, email: string, fullName: string) => {
    localStorage.setItem("token", token);
    localStorage.setItem("email", email);
    localStorage.setItem("fullName", fullName);
    setUser({ token, email, fullName });
  };

  /**
   * Clears all stored credentials and redirects to the login page.
   * Called when the user clicks "Logout" or when a 401 is detected.
   */
  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    localStorage.removeItem("fullName");
    setUser(null);

    // Redirect to login page
    if (typeof window !== "undefined") {
      window.location.href = "/login";
    }
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Hook to access auth state from any component.
 * Throws if used outside an AuthProvider — this is intentional
 * because it means we forgot to wrap the component tree.
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
