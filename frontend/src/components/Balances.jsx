import { useEffect, useState } from "react";
import { fetchBalances, fetchSettlements, recordSettlement, fetchSettlementHistory } from "../api/balanceApi";
import {
  Box, Typography, List, ListItem, ListItemText, Divider, Button, Alert, Stack,
} from "@mui/material";
import CheckCircleOutlineRoundedIcon from "@mui/icons-material/CheckCircleOutlineRounded";
import HistoryRoundedIcon from "@mui/icons-material/HistoryRounded";
import EmojiEventsRoundedIcon from "@mui/icons-material/EmojiEventsRounded";
import AmountChip from "./AmountChip";

export default function Balances({ groupId, refreshKey }) {
  const [balances, setBalances] = useState([]);
  const [settlements, setSettlements] = useState([]);
  const [history, setHistory] = useState([]);
  const [error, setError] = useState("");

  const load = () => {
    fetchBalances(groupId).then(setBalances);
    fetchSettlements(groupId).then(setSettlements);
    fetchSettlementHistory(groupId).then(setHistory);
  };

  useEffect(() => { load(); }, [groupId, refreshKey]);

  const handleSettle = async (toUserId, amount) => {
    setError("");
    try {
      await recordSettlement(groupId, toUserId, amount);
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not record settlement");
    }
  };

  return (
    <Box>
      <Typography variant="h6" sx={{ mb: 1 }}>Balances</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <List disablePadding sx={{ mb: 3 }}>
        {balances.map((b, i) => (
          <Box key={b.userId}>
            {i > 0 && <Divider component="li" />}
            <ListItem disablePadding sx={{ py: 1 }} secondaryAction={<AmountChip value={b.netBalance} />}>
              <ListItemText primary={b.name} />
            </ListItem>
          </Box>
        ))}
      </List>

      <Typography variant="h6" sx={{ mb: 1 }}>Suggested Settlements</Typography>
      {settlements.length === 0 && (
        <Stack direction="row" spacing={1} sx={{ alignItems: "center", color: "var(--color-green)", mb: 2 }}>
          <EmojiEventsRoundedIcon fontSize="small" />
          <Typography>Everyone is settled up.</Typography>
        </Stack>
      )}
      <List disablePadding sx={{ mb: 3 }}>
        {settlements.map((s, i) => (
          <Box key={i}>
            {i > 0 && <Divider component="li" />}
            <ListItem
              disablePadding sx={{ py: 1 }}
              secondaryAction={
                <Stack direction="row" spacing={1.5} sx={{ alignItems: "center" }}>
                  <AmountChip value={-s.amount} />
                  <Button
                    size="small" variant="outlined" startIcon={<CheckCircleOutlineRoundedIcon />}
                    onClick={() => handleSettle(s.toUserId, s.amount)}
                    sx={{ borderColor: "var(--color-green)", color: "var(--color-green)", "&:hover": { bgcolor: "var(--color-green-bg)", borderColor: "var(--color-green)" } }}
                  >
                    Mark as Paid
                  </Button>
                </Stack>
              }
            >
              <ListItemText primary={`${s.fromName} → ${s.toName}`} />
            </ListItem>
          </Box>
        ))}
      </List>

      <Typography variant="h6" sx={{ mb: 1, display: "flex", alignItems: "center", gap: 1 }}>
        <HistoryRoundedIcon fontSize="small" /> Settlement History
      </Typography>
      {history.length === 0 && <Typography color="text.secondary">No settlements recorded yet.</Typography>}
      <List disablePadding>
        {history.map((h, i) => (
          <Box key={h.id}>
            {i > 0 && <Divider component="li" />}
            <ListItem
              disablePadding sx={{ py: 1 }}
              secondaryAction={
                <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
                  <AmountChip value={-h.amount} />
                  <Typography variant="caption" color="text.secondary">{new Date(h.settledAt).toLocaleDateString()}</Typography>
                </Stack>
              }
            >
              <ListItemText primary={`${h.fromName} paid ${h.toName}`} />
            </ListItem>
          </Box>
        ))}
      </List>
    </Box>
  );
}