import { create } from "zustand";
import { persist } from "zustand/middleware";

const getSystemDefault = () =>
  window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";

export const useThemeStore = create(
  persist(
    (set, get) => ({
      mode: getSystemDefault(), // "light" | "dark" — overwritten by persisted value if one exists
      toggle: () => set({ mode: get().mode === "light" ? "dark" : "light" }),
      setMode: (mode) => set({ mode }),
    }),
    { name: "theme-storage" }
  )
);