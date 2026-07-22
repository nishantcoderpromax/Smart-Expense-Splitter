import { useEffect, useState } from "react";
import { useSearchParams, Link } from "react-router-dom";
import { verifyEmail } from "../api/authApi";
import { Box, Typography, CircularProgress, Alert, Button } from "@mui/material";
import CheckCircleOutlineRoundedIcon from "@mui/icons-material/CheckCircleOutlineRounded";

export default function VerifyEmail() {
  const [params] = useSearchParams();
  const [status, setStatus] = useState("loading"); // loading | success | error
  const [error, setError] = useState("");

  useEffect(() => {
    const token = params.get("token");
    if (!token) { setStatus("error"); setError("Missing verification token."); return; }

    verifyEmail(token)
      .then(() => setStatus("success"))
      .catch((err) => {
        setStatus("error");
        setError(err.response?.data?.message || "Could not verify this email.");
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Box sx={{ minHeight: "100%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 2, px: 2 }}>
      {status === "loading" && <CircularProgress sx={{ color: "var(--color-brass)" }} />}

      {status === "success" && (
        <>
          <CheckCircleOutlineRoundedIcon sx={{ fontSize: 48, color: "var(--color-green)" }} />
          <Typography variant="h5">Email verified</Typography>
          <Typography color="text.secondary">Your email address has been confirmed.</Typography>
          <Button component={Link} to="/dashboard" variant="contained" sx={{ mt: 1 }}>Go to Dashboard</Button>
        </>
      )}

      {status === "error" && (
        <>
          <Alert severity="error" sx={{ maxWidth: 400 }}>{error}</Alert>
          <Button component={Link} to="/dashboard" variant="outlined" sx={{ mt: 1 }}>Go to Dashboard</Button>
        </>
      )}
    </Box>
  );
}
