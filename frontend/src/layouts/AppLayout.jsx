import { Link, Outlet, useNavigate, useLocation } from "react-router-dom";
import { AppBar, Toolbar, Box, Button, Container, IconButton, Tooltip } from "@mui/material";
import DashboardRoundedIcon from "@mui/icons-material/DashboardRounded";
import GroupsRoundedIcon from "@mui/icons-material/GroupsRounded";
import PersonRoundedIcon from "@mui/icons-material/PersonRounded";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import LightModeRoundedIcon from "@mui/icons-material/LightModeRounded";
import DarkModeRoundedIcon from "@mui/icons-material/DarkModeRounded";
import InstallMobileRoundedIcon from "@mui/icons-material/InstallMobileRounded";
import { useAuthStore } from "../store/authStore";
import { useThemeStore } from "../store/themeStore";
import { useInstallPrompt } from "../hooks/useInstallPrompt";

const navItems = [
  { to: "/dashboard", label: "Dashboard", icon: <DashboardRoundedIcon fontSize="small" /> },
  { to: "/groups", label: "Groups", icon: <GroupsRoundedIcon fontSize="small" /> },
  { to: "/profile", label: "Profile", icon: <PersonRoundedIcon fontSize="small" /> },
];

export default function AppLayout() {
  const clearAuth = useAuthStore((s) => s.clearAuth);
  const mode = useThemeStore((s) => s.mode);
  const toggleTheme = useThemeStore((s) => s.toggle);
  const navigate = useNavigate();
  const location = useLocation();
  const { canInstall, promptInstall } = useInstallPrompt();

  const handleLogout = () => {
    clearAuth();
    navigate("/login");
  };

  return (
    <Box sx={{ minHeight: "100%" }}>
      <AppBar
        position="sticky"
        elevation={0}
        sx={{
          backdropFilter: "blur(10px)",
          borderBottom: "1px solid var(--color-rule)",
          color: "var(--color-ink)",
        }}
      >
        <Toolbar sx={{ maxWidth: 960, width: "100%", mx: "auto", px: { xs: 2, sm: 3 } }}>
          <Box
            component={Link}
            to="/dashboard"
            sx={{
              fontFamily: "'Fraunces', serif",
              fontWeight: 600,
              fontSize: "1.3rem",
              color: "var(--color-ink)",
              flexGrow: 1,
              textDecoration: "none",
            }}
          >
            Split<span style={{ color: "var(--color-brass)" }}>Ledger</span>
          </Box>

          <Box sx={{ display: "flex", gap: 0.5, alignItems: "center" }}>
            {navItems.map((item) => {
              const active = location.pathname.startsWith(item.to);
              return (
                <Button
                  key={item.to}
                  component={Link}
                  to={item.to}
                  startIcon={item.icon}
                  sx={{
                    color: active ? "var(--color-brass-dark)" : "var(--color-ink-soft)",
                    fontWeight: active ? 700 : 500,
                    bgcolor: active ? "rgba(184,137,43,0.08)" : "transparent",
                    "&:hover": { bgcolor: "rgba(184,137,43,0.12)", color: "var(--color-brass-dark)" },
                  }}
                >
                  {item.label}
                </Button>
              );
            })}

            {canInstall && (
              <Tooltip title="Install SplitLedger as an app">
                <IconButton
                  onClick={promptInstall}
                  sx={{
                    color: "var(--color-ink-soft)",
                    "&:hover": { bgcolor: "rgba(184,137,43,0.12)", color: "var(--color-brass-dark)" },
                  }}
                >
                  <InstallMobileRoundedIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            )}

            <Tooltip title={mode === "light" ? "Switch to dark mode" : "Switch to light mode"}>
              <IconButton
                onClick={toggleTheme}
                sx={{
                  color: "var(--color-ink-soft)",
                  ml: 0.5,
                  "&:hover": { bgcolor: "rgba(184,137,43,0.12)", color: "var(--color-brass-dark)", transform: "rotate(20deg)" },
                }}
              >
                {mode === "light" ? <DarkModeRoundedIcon fontSize="small" /> : <LightModeRoundedIcon fontSize="small" />}
              </IconButton>
            </Tooltip>

            <Button
              onClick={handleLogout}
              startIcon={<LogoutRoundedIcon fontSize="small" />}
              sx={{
                color: "var(--color-ink-soft)",
                "&:hover": { bgcolor: "rgba(178,58,46,0.08)", color: "var(--color-red)" },
              }}
            >
              Logout
            </Button>
          </Box>
        </Toolbar>
      </AppBar>

      <Container maxWidth="md" sx={{ py: 4, pb: 10 }}>
        <Outlet />
      </Container>
    </Box>
  );
}