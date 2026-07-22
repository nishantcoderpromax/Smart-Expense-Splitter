import { useEffect, useMemo, Suspense, lazy } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { ThemeProvider, CssBaseline, Box, CircularProgress } from "@mui/material";
import getTheme from "./styles/muiTheme.js";
import { useThemeStore } from "./store/themeStore";
import ProtectedRoute from "./routes/ProtectedRoute";
import AppLayout from "./layouts/AppLayout";

// Route-level code splitting: each page becomes its own chunk, fetched only
// when actually navigated to, instead of all pages bundled into one big
// upfront download. Auth pages (Login/Register/etc.) are the very first thing
// a new visitor loads, so keeping those small matters most.
const Login = lazy(() => import("./pages/Login"));
const Register = lazy(() => import("./pages/Register"));
const OAuth2Redirect = lazy(() => import("./pages/OAuth2Redirect"));
const VerifyEmail = lazy(() => import("./pages/VerifyEmail"));
const ForgotPassword = lazy(() => import("./pages/ForgotPassword"));
const ResetPassword = lazy(() => import("./pages/ResetPassword"));
const Dashboard = lazy(() => import("./pages/Dashboard"));
const Groups = lazy(() => import("./pages/Groups"));
const GroupDetail = lazy(() => import("./pages/GroupDetail")); // the heaviest page — charts, timeline, comments, recurring expenses
const JoinGroup = lazy(() => import("./pages/JoinGroup"));
const Profile = lazy(() => import("./pages/Profile"));

function PageLoadingFallback() {
  return (
    <Box sx={{ minHeight: "60vh", display: "flex", alignItems: "center", justifyContent: "center" }}>
      <CircularProgress sx={{ color: "var(--color-brass)" }} />
    </Box>
  );
}

export default function App() {
  const mode = useThemeStore((s) => s.mode);
  const muiTheme = useMemo(() => getTheme(mode), [mode]);

  // Drives the CSS custom properties in theme.css ([data-theme="dark"] overrides)
  useEffect(() => {
    document.documentElement.setAttribute("data-theme", mode);
  }, [mode]);

  return (
    <ThemeProvider theme={muiTheme}>
      <CssBaseline />
      <BrowserRouter>
        <Suspense fallback={<PageLoadingFallback />}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
            <Route path="/verify-email" element={<VerifyEmail />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />

            <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/groups" element={<Groups />} />
                <Route path="/groups/:groupId" element={<GroupDetail />} />
                <Route path="/join/:token" element={<JoinGroup />} />
                <Route path="/profile" element={<Profile />} />
              </Route>
            </Route>

            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Suspense>
      </BrowserRouter>
    </ThemeProvider>
  );
}