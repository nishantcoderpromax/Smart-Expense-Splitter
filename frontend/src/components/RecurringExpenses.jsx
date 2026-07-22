import { useEffect, useState } from "react";
import {
  fetchRecurringExpenses, createRecurringExpense, setRecurringExpenseActive,
  deleteRecurringExpense, runRecurringExpenseNow,
} from "../api/recurringExpenseApi";
import { fetchCategories } from "../api/categoryApi";
import {
  Box, Typography, TextField, Select, MenuItem, FormControl, InputLabel,
  Button, List, ListItem, ListItemText, IconButton, Chip, Stack, Alert, Switch,
} from "@mui/material";
import DeleteOutlineRoundedIcon from "@mui/icons-material/DeleteOutlineRounded";
import PlayCircleOutlineRoundedIcon from "@mui/icons-material/PlayCircleOutlineRounded";
import RepeatRoundedIcon from "@mui/icons-material/RepeatRounded";

const FREQUENCIES = ["WEEKLY", "MONTHLY", "YEARLY"];

export default function RecurringExpenses({ groupId, members, onGenerated }) {
  const [list, setList] = useState([]);
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({
    description: "", amount: "", paidBy: members[0]?.userId ?? "",
    categoryId: "", frequency: "MONTHLY", startDate: new Date().toISOString().slice(0, 10),
  });
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const load = () => fetchRecurringExpenses(groupId).then(setList);

  useEffect(() => { load(); fetchCategories().then(setCategories); }, [groupId]);

  const handleCreate = async (e) => {
    e.preventDefault();
    setError("");
    try {
      // EQUAL split among all current members — simplest common case for rent/subscriptions
      const participants = members.map((m) => ({ userId: m.userId, value: null }));
      await createRecurringExpense(groupId, {
        description: form.description,
        amount: Number(form.amount),
        splitType: "EQUAL",
        paidBy: Number(form.paidBy),
        categoryId: form.categoryId ? Number(form.categoryId) : null,
        frequency: form.frequency,
        startDate: form.startDate,
        participants,
      });
      setForm({ ...form, description: "", amount: "" });
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not create recurring expense");
    }
  };

  const handleToggle = async (id, active) => {
    await setRecurringExpenseActive(groupId, id, active);
    load();
  };

  const handleDelete = async (id) => {
    await deleteRecurringExpense(groupId, id);
    load();
  };

  const handleRunNow = async (id) => {
    setMessage("");
    await runRecurringExpenseNow(groupId, id);
    setMessage("Generated this cycle's expense.");
    load();
    onGenerated(); // refresh expense list/balances elsewhere on the page
  };

  return (
    <Box>
      <Typography variant="h6" sx={{ mb: 2, display: "flex", alignItems: "center", gap: 1 }}>
        <RepeatRoundedIcon fontSize="small" /> Recurring Expenses
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {message && <Alert severity="success" sx={{ mb: 2 }}>{message}</Alert>}

      <Box component="form" onSubmit={handleCreate} sx={{ mb: 3 }}>
        <Stack direction="row" spacing={1.5} sx={{ flexWrap: "wrap", mb: 1.5 }}>
          <TextField
            label="Description" value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            required sx={{ flex: 2, minWidth: 180 }}
          />
          <TextField
            label="Amount" type="number" value={form.amount}
            onChange={(e) => setForm({ ...form, amount: e.target.value })}
            required sx={{ flex: 1, minWidth: 120 }}
          />
          <FormControl sx={{ flex: 1, minWidth: 160 }}>
            <InputLabel>Paid by</InputLabel>
            <Select label="Paid by" value={form.paidBy} onChange={(e) => setForm({ ...form, paidBy: e.target.value })}>
              {members.map((m) => <MenuItem key={m.userId} value={m.userId}>{m.name}</MenuItem>)}
            </Select>
          </FormControl>
        </Stack>

        <Stack direction="row" spacing={1.5} sx={{ flexWrap: "wrap", mb: 1.5 }}>
          <FormControl sx={{ flex: 1, minWidth: 160 }}>
            <InputLabel>Category</InputLabel>
            <Select label="Category" value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
              <MenuItem value="">No category</MenuItem>
              {categories.map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl sx={{ flex: 1, minWidth: 140 }}>
            <InputLabel>Frequency</InputLabel>
            <Select label="Frequency" value={form.frequency} onChange={(e) => setForm({ ...form, frequency: e.target.value })}>
              {FREQUENCIES.map((f) => <MenuItem key={f} value={f}>{f}</MenuItem>)}
            </Select>
          </FormControl>
          <TextField
            label="Starts on" type="date" value={form.startDate}
            onChange={(e) => setForm({ ...form, startDate: e.target.value })}
            slotProps={{ inputLabel: { shrink: true } }}
            sx={{ flex: 1, minWidth: 160 }}
          />
        </Stack>

        <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
          Split equally among all {members.length} current members each cycle.
        </Typography>

        <Button type="submit" variant="contained">Set Up Recurring Expense</Button>
      </Box>

      <List disablePadding>
        {list.map((r) => (
          <ListItem
            key={r.id}
            disablePadding
            sx={{ py: 1, borderBottom: "1px solid var(--color-rule)" }}
            secondaryAction={
              <Stack direction="row" spacing={0.5} sx={{ alignItems: "center" }}>
                <Switch size="small" checked={r.active} onChange={(e) => handleToggle(r.id, e.target.checked)} />
                <IconButton size="small" onClick={() => handleRunNow(r.id)} title="Run now" sx={{ "&:hover": { color: "var(--color-brass-dark)" } }}>
                  <PlayCircleOutlineRoundedIcon fontSize="small" />
                </IconButton>
                <IconButton size="small" onClick={() => handleDelete(r.id)} sx={{ "&:hover": { color: "var(--color-red)" } }}>
                  <DeleteOutlineRoundedIcon fontSize="small" />
                </IconButton>
              </Stack>
            }
          >
            <ListItemText
              primary={<span style={{ fontWeight: 600 }}>{r.description}</span>}
              secondary={
                <>
                  {Number(r.amount).toFixed(2)} · paid by {r.paidByName}
                  {r.categoryName ? ` · ${r.categoryName}` : ""}
                  {" · "}<Chip label={r.frequency} size="small" sx={{ height: 18, fontSize: "0.7rem" }} />
                  {" · next: "}{r.nextRunDate}
                  {!r.active && " · paused"}
                </>
              }
            />
          </ListItem>
        ))}
        {list.length === 0 && <Typography color="text.secondary">No recurring expenses set up yet.</Typography>}
      </List>
    </Box>
  );
}