import { useEffect, useState, lazy, Suspense } from "react";
import { useParams } from "react-router-dom";
import { fetchGroup, addMember, removeMember } from "../api/groupApi";
import { fetchExpenses } from "../api/expenseApi";
import AddExpenseForm from "../components/AddExpenseForm";
import AddMemberAutocomplete from "../components/AddMemberAutocomplete";
import ExpenseSearch from "../components/ExpenseSearch";
import Balances from "../components/Balances";
import ActivityTimeline from "../components/ActivityTimeline";
import RecurringExpenses from "../components/RecurringExpenses";
import InviteLinkSection from "../components/InviteLinkSection";
import { useAuthStore } from "../store/authStore";
import { downloadCsv, downloadPdf } from "../api/exportApi";
import { useGroupSocket } from "../hooks/useGroupSocket";
import {
  Box, Typography, Card, CardContent, Button, Avatar, Chip, Stack, Alert,
  List, ListItem, ListItemText, IconButton, Divider, Snackbar, CircularProgress,
} from "@mui/material";
import PersonRemoveRoundedIcon from "@mui/icons-material/PersonRemoveRounded";
import FileDownloadRoundedIcon from "@mui/icons-material/FileDownloadRounded";
import PictureAsPdfRoundedIcon from "@mui/icons-material/PictureAsPdfRounded";
import SensorsRoundedIcon from "@mui/icons-material/SensorsRounded";

// Recharts is the single heaviest dependency on this page — split into its own
// chunk so it's only fetched once someone actually scrolls to the charts
// section, instead of blocking the rest of the group page from rendering.
const Charts = lazy(() => import("../components/Charts"));

export default function GroupDetail() {
  const { groupId } = useParams();
  const [group, setGroup] = useState(null);
  const [error, setError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [liveMessage, setLiveMessage] = useState("");
  const currentUserEmail = useAuthStore((s) => s.user?.email);
  const isAdmin = group?.members?.some((m) => m.email === currentUserEmail && m.role === "ADMIN") ?? false;

  const loadGroup = () => fetchGroup(groupId).then(setGroup).catch(() => setError("Could not load group"));
  const loadExpenses = () => {
    fetchExpenses(groupId).catch(() => setError("Could not load expenses"));
    setRefreshKey((k) => k + 1); // tells Balances/Charts to re-fetch
  };

  useEffect(() => { loadGroup(); loadExpenses(); }, [groupId]);

  const EVENT_LABELS = {
    EXPENSE_ADDED: "An expense was added",
    EXPENSE_DELETED: "An expense was removed",
    MEMBER_ADDED: "A member joined the group",
    MEMBER_REMOVED: "A member was removed",
    SETTLEMENT_RECORDED: "A settlement was recorded",
  };

  // Live updates: whenever anyone (including you, from another tab) changes
  // this group, refetch over REST rather than trusting the socket payload.
  useGroupSocket(groupId, (event) => {
    loadGroup();
    loadExpenses();
    setLiveMessage(EVENT_LABELS[event.type] || "Group updated");
  });

  const handleAddMember = async (user) => {
    setError("");
    try {
      await addMember(groupId, user.email);
      loadGroup();
    } catch (err) {
      setError(err.response?.data?.message || "Could not add member");
    }
  };

  const handleRemoveMember = async (userId) => {
    try {
      await removeMember(groupId, userId);
      loadGroup();
    } catch (err) {
      setError(err.response?.data?.message || "Could not remove member");
    }
  };

  if (!group) return <Typography color="text.secondary">Loading...</Typography>;

  return (
    <Box>
      <Stack direction="row" sx={{ justifyContent: "space-between", alignItems: "flex-start", flexWrap: "wrap", gap: 1, mb: 2 }}>
        <Box>
          <Typography variant="h4" sx={{ mb: 0.5 }}>{group.name}</Typography>
          <Typography color="text.secondary">{group.description}</Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" size="small" startIcon={<FileDownloadRoundedIcon />} onClick={() => downloadCsv(groupId)}>CSV</Button>
          <Button variant="outlined" size="small" startIcon={<PictureAsPdfRoundedIcon />} onClick={() => downloadPdf(groupId)}>PDF</Button>
        </Stack>
      </Stack>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card sx={{ mb: 3, borderRadius: 3 }}>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 1 }}>Members</Typography>
          <List disablePadding>
            {group.members.map((m, i) => (
              <Box key={m.userId}>
                {i > 0 && <Divider component="li" />}
                <ListItem
                  disablePadding sx={{ py: 1 }}
                  secondaryAction={
                    <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
                      <Chip
                        label={m.role} size="small"
                        sx={{
                          fontWeight: 700,
                          bgcolor: m.role === "ADMIN" ? "var(--color-green-bg)" : "var(--color-rule)",
                          color: m.role === "ADMIN" ? "var(--color-green)" : "var(--color-ink-soft)",
                        }}
                      />
                      {m.role !== "ADMIN" && (
                        <IconButton size="small" onClick={() => handleRemoveMember(m.userId)} sx={{ "&:hover": { color: "var(--color-red)", bgcolor: "var(--color-red-bg)" } }}>
                          <PersonRemoveRoundedIcon fontSize="small" />
                        </IconButton>
                      )}
                    </Stack>
                  }
                >
                  <Stack direction="row" spacing={1.5} sx={{ alignItems: "center" }}>
                    <Avatar sx={{ width: 32, height: 32, bgcolor: "var(--color-brass)", fontSize: "0.85rem" }}>
                      {m.name?.[0]?.toUpperCase()}
                    </Avatar>
                    <ListItemText primary={m.name} secondary={m.email} />
                  </Stack>
                </ListItem>
              </Box>
            ))}
          </List>

          <Box sx={{ mt: 2 }}>
            <AddMemberAutocomplete onSelect={handleAddMember} />
          </Box>

          <InviteLinkSection groupId={groupId} isAdmin={isAdmin} />
        </CardContent>
      </Card>

      <Card sx={{ mb: 3, borderRadius: 3 }}>
        <CardContent>
          <AddExpenseForm groupId={groupId} members={group.members} onAdded={loadExpenses} />
        </CardContent>
      </Card>

      <Card sx={{ mb: 3, borderRadius: 3 }}>
        <CardContent>
          <ExpenseSearch groupId={groupId} refreshKey={refreshKey} onChanged={loadExpenses} />
        </CardContent>
      </Card>

      <Card sx={{ mb: 3, borderRadius: 3 }}>
        <CardContent>
          <RecurringExpenses groupId={groupId} members={group.members} onGenerated={loadExpenses} />
        </CardContent>
      </Card>

      <Card sx={{ mb: 3, borderRadius: 3 }}>
        <CardContent>
          <Balances groupId={groupId} refreshKey={refreshKey} />
        </CardContent>
      </Card>

      <Card sx={{ mb: 3, borderRadius: 3 }}>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 1 }}>Activity</Typography>
          <ActivityTimeline groupId={groupId} refreshKey={refreshKey} />
        </CardContent>
      </Card>

      <Card sx={{ borderRadius: 3 }}>
        <CardContent>
          <Suspense fallback={<Box sx={{ display: "flex", justifyContent: "center", py: 4 }}><CircularProgress size={24} sx={{ color: "var(--color-brass)" }} /></Box>}>
            <Charts groupId={groupId} refreshKey={refreshKey} />
          </Suspense>
        </CardContent>
      </Card>

      <Snackbar
        open={!!liveMessage}
        autoHideDuration={3000}
        onClose={() => setLiveMessage("")}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
        message={
          <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
            <SensorsRoundedIcon fontSize="small" sx={{ color: "var(--color-green)" }} />
            <span>{liveMessage}</span>
          </Stack>
        }
      />
    </Box>
  );
}