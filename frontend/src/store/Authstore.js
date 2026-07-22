import { create } from "zustand";
import { persist } from "zustand/middleware";
 
export const useAuthStore = create(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
 
      setAuth: (data) =>
        set({
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
          user: { name: data.name, email: data.email },
        }),
 
      clearAuth: () => set({ accessToken: null, refreshToken: null, user: null }),
    }),
    { name: "auth-storage" } // persisted key (localStorage, via zustand middleware)
  )
);