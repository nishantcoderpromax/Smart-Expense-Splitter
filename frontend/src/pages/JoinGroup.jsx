import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { joinGroupViaInvite } from "../api/groupApi";
import { Box, Typography, CircularProgress, Alert, Button } from "@mui/material";
import GroupsRoundedIcon from "@mui/icons-material/GroupsRounded";

export default function JoinGroup() {
  const { token } = useParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState("loading"); // loading | success | error
  const [error, setError] = useState("");
  const [groupName, setGroupName] = useState("");

  useEffect(() => {
    joinGroupViaInvite(token)
      .then((group) => {
        setGroupName(group.name);
        setStatus("success");
        setTimeout(() => navigate(`/groups/${group.id}`, { replace: true }), 1500);
      })
      .catch((err) => {
        setStatus("error");
        setError(err.response?.data?.message || "This invite link is invalid or has expired.");
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  return (
    <Box sx={{ minHeight: "60vh", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 2, px: 2 }}>
      {status === "loading" && (
        <>
          <CircularProgress sx={{ color: "var(--color-brass)" }} />
          <Typography color="text.secondary">Joining group...</Typography>
        </>
      )}

      {status === "success" && (
        <>
          <GroupsRoundedIcon sx={{ fontSize: 48, color: "var(--color-green)" }} />
          <Typography variant="h5">You're in!</Typography>
          <Typography color="text.secondary">Joined "{groupName}" — taking you there now...</Typography>
        </>
      )}

      {status === "error" && (
        <>
          <Alert severity="error" sx={{ maxWidth: 420 }}>{error}</Alert>
          <Button component={Link} to="/groups" variant="outlined">Go to My Groups</Button>
        </>
      )}
    </Box>
  );
}