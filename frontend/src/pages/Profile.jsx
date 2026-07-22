import { useEffect, useState } from "react";
import { fetchProfile, updateProfile } from "../api/userApi";
import { resendVerification } from "../api/authApi";
import { Box, Card, CardContent, Typography, TextField, Button, Alert, Stack, Avatar, Divider, Chip } from "@mui/material";
import PersonRoundedIcon from "@mui/icons-material/PersonRounded";
import SaveRoundedIcon from "@mui/icons-material/SaveRounded";
import VerifiedRoundedIcon from "@mui/icons-material/VerifiedRounded";
import ErrorOutlineRoundedIcon from "@mui/icons-material/ErrorOutlineRounded";

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [name, setName] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [resendSent, setResendSent] = useState(false);

  useEffect(() => {
    fetchProfile().then((p) => { setProfile(p); setName(p.name); });
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(""); setMessage(""); setLoading(true);
    try {
      const updated = await updateProfile({ name, currentPassword, newPassword });
      setProfile(updated);
      setCurrentPassword(""); setNewPassword("");
      setMessage("Profile updated.");
    } catch (err) {
      setError(err.response?.data?.message || "Could not update profile");
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    await resendVerification();
    setResendSent(true);
  };

  if (!profile) return <Typography color="text.secondary">Loading...</Typography>;

  return (
    <Box sx={{ maxWidth: 480 }}>
      <Typography variant="h4" sx={{ mb: 3 }}>Profile</Typography>

      <Card sx={{ borderRadius: 3 }}>
        <CardContent>
          <Stack direction="row" spacing={2} sx={{ alignItems: "center", mb: 2 }}>
            <Avatar sx={{ bgcolor: "var(--color-brass)", width: 56, height: 56 }}>
              <PersonRoundedIcon fontSize="large" />
            </Avatar>
            <Box>
              <Typography sx={{ fontWeight: 600 }}>{profile.name}</Typography>
              <Typography variant="body2" color="text.secondary">{profile.email}</Typography>
            </Box>
          </Stack>

          {profile.emailVerified ? (
            <Chip
              icon={<VerifiedRoundedIcon />} label="Email verified" size="small"
              sx={{ bgcolor: "var(--color-green-bg)", color: "var(--color-green)", mb: 2, "& .MuiChip-icon": { color: "inherit" } }}
            />
          ) : (
            <Box sx={{ mb: 2 }}>
              <Chip
                icon={<ErrorOutlineRoundedIcon />} label="Email not verified" size="small"
                sx={{ bgcolor: "var(--color-red-bg)", color: "var(--color-red)", mb: 1, "& .MuiChip-icon": { color: "inherit" } }}
              />
              <br />
              {resendSent ? (
                <Typography variant="body2" color="text.secondary">Verification email sent — check your inbox.</Typography>
              ) : (
                <Button size="small" onClick={handleResend}>Resend verification email</Button>
              )}
            </Box>
          )}

          <Divider sx={{ mb: 2 }} />
          {message && <Alert severity="success" sx={{ mb: 2 }}>{message}</Alert>}
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

          <Box component="form" onSubmit={handleSubmit}>
            <Stack spacing={2}>
              <TextField label="Name" value={name} onChange={(e) => setName(e.target.value)} fullWidth />

              <Typography variant="subtitle2" color="text.secondary" sx={{ pt: 1 }}>Change Password (optional)</Typography>
              <TextField label="Current password" type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} fullWidth />
              <TextField label="New password" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} fullWidth />

              <Button type="submit" variant="contained" disabled={loading} startIcon={<SaveRoundedIcon />} sx={{ alignSelf: "flex-start" }}>
                Save Changes
              </Button>
            </Stack>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
