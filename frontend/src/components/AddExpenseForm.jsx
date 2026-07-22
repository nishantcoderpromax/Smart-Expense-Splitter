import { useEffect, useState } from "react";
import { addExpense } from "../api/expenseApi";
import { fetchCategories } from "../api/categoryApi";
import {
  Box, TextField, Select, MenuItem, ToggleButtonGroup, ToggleButton,
  Button, Typography, Alert, InputLabel, FormControl, Stack,
} from "@mui/material";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import ReceiptScanner from "./ReceiptScanner";

const SPLIT_TYPES = ["EQUAL", "UNEQUAL", "PERCENTAGE", "SHARES"];

export default function AddExpenseForm({ groupId, members, onAdded }) {
  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");
  const [splitType, setSplitType] = useState("EQUAL");
  const [paidBy, setPaidBy] = useState(members[0]?.userId ?? "");
  const [categoryId, setCategoryId] = useState("");
  const [categories, setCategories] = useState([]);
  const [values, setValues] = useState({});
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => { fetchCategories().then(setCategories); }, []);

  const handleReceiptExtracted = ({ amount: extractedAmount, description: extractedDescription }) => {
    if (extractedAmount != null) setAmount(String(extractedAmount));
    if (extractedDescription) setDescription(extractedDescription);
  };

  const valueLabel = {
    EQUAL: null,
    UNEQUAL: "Exact amount owed",
    PERCENTAGE: "% owed",
    SHARES: "Shares",
  }[splitType];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(""); setLoading(true);
    try {
      const participants = members.map((m) => ({
        userId: m.userId,
        value: splitType === "EQUAL" ? null : Number(values[m.userId] || 0),
      }));

      await addExpense(groupId, {
        description,
        amount: Number(amount),
        splitType,
        paidBy: Number(paidBy),
        categoryId: categoryId ? Number(categoryId) : null,
        participants,
      });

      setDescription(""); setAmount(""); setValues({}); setCategoryId("");
      onAdded();
    } catch (err) {
      setError(err.response?.data?.message || "Could not add expense");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box component="form" onSubmit={handleSubmit}>
      <Typography variant="h6" sx={{ mb: 2 }}>Add Expense</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <ReceiptScanner onExtracted={handleReceiptExtracted} />

      <Stack spacing={2}>
        <Stack direction="row" spacing={2} sx={{ flexWrap: "wrap" }}>
          <TextField label="Description" value={description} onChange={(e) => setDescription(e.target.value)} required sx={{ flex: 2, minWidth: 200 }} />
          <TextField label="Total amount" type="number" slotProps={{ htmlInput: { step: "0.01" } }} value={amount} onChange={(e) => setAmount(e.target.value)} required sx={{ flex: 1, minWidth: 140 }} />
        </Stack>

        <Stack direction="row" spacing={2} sx={{ flexWrap: "wrap" }}>
          <FormControl sx={{ flex: 1, minWidth: 180 }}>
            <InputLabel>Paid by</InputLabel>
            <Select label="Paid by" value={paidBy} onChange={(e) => setPaidBy(e.target.value)}>
              {members.map((m) => <MenuItem key={m.userId} value={m.userId}>{m.name}</MenuItem>)}
            </Select>
          </FormControl>

          <FormControl sx={{ flex: 1, minWidth: 180 }}>
            <InputLabel>Category</InputLabel>
            <Select label="Category" value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
              <MenuItem value="">No category</MenuItem>
              {categories.map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
            </Select>
          </FormControl>
        </Stack>

        <Box>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>Split method</Typography>
          <ToggleButtonGroup
            value={splitType}
            exclusive
            onChange={(_e, val) => val && setSplitType(val)}
            size="small"
            sx={{
              "& .MuiToggleButton-root": {
                textTransform: "none", fontWeight: 600, px: 2,
                "&.Mui-selected": { bgcolor: "var(--color-brass)", color: "#fff", "&:hover": { bgcolor: "var(--color-brass-dark)" } },
              },
            }}
          >
            {SPLIT_TYPES.map((t) => <ToggleButton key={t} value={t}>{t}</ToggleButton>)}
          </ToggleButtonGroup>
        </Box>

        {valueLabel && (
          <Box sx={{ border: "1px dashed var(--color-rule)", borderRadius: 2, p: 2 }}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>{valueLabel} per person</Typography>
            <Stack spacing={1.5}>
              {members.map((m) => (
                <Stack direction="row" spacing={2} sx={{ alignItems: "center" }} key={m.userId}>
                  <Typography sx={{ minWidth: 100 }}>{m.name}</Typography>
                  <TextField
                    size="small" type="number" slotProps={{ htmlInput: { step: "0.01" } }}
                    value={values[m.userId] || ""}
                    onChange={(e) => setValues({ ...values, [m.userId]: e.target.value })}
                  />
                </Stack>
              ))}
            </Stack>
          </Box>
        )}
        {splitType === "EQUAL" && (
          <Typography variant="body2" color="text.secondary">
            Split equally among all {members.length} members.
          </Typography>
        )}

        <Button type="submit" variant="contained" disabled={loading} startIcon={<AddRoundedIcon />} sx={{ alignSelf: "flex-start" }}>
          Add Expense
        </Button>
      </Stack>
    </Box>
  );
}
