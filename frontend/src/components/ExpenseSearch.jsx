import { useEffect, useState } from "react";
import { searchExpenses, deleteExpense } from "../api/expenseApi";
import { fetchCategories } from "../api/categoryApi";
import {
  Box, TextField, Select, MenuItem, FormControl, InputLabel, IconButton,
  Typography, Stack, List, ListItem, ListItemText, Chip, Pagination, Divider,
  Dialog, DialogTitle, DialogContent,
} from "@mui/material";
import DeleteOutlineRoundedIcon from "@mui/icons-material/DeleteOutlineRounded";
import SearchRoundedIcon from "@mui/icons-material/SearchRounded";
import ChatBubbleOutlineRoundedIcon from "@mui/icons-material/ChatBubbleOutlineRounded";
import InputAdornment from "@mui/material/InputAdornment";
import ExpenseComments from "./ExpenseComments";

export default function ExpenseSearch({ groupId, refreshKey, onChanged }) {
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState({ description: "", categoryId: "", minAmount: "", maxAmount: "" });
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDir, setSortDir] = useState("desc");
  const [page, setPage] = useState(0);
  const [result, setResult] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [commentsExpenseId, setCommentsExpenseId] = useState(null);

  useEffect(() => { fetchCategories().then(setCategories); }, []);

  const load = () => {
    const params = {
      ...filters,
      categoryId: filters.categoryId || undefined,
      minAmount: filters.minAmount || undefined,
      maxAmount: filters.maxAmount || undefined,
      description: filters.description || undefined,
      sortBy, sortDir, page, size: 5,
    };
    searchExpenses(groupId, params).then(setResult);
  };

  useEffect(() => { load(); }, [groupId, refreshKey, sortBy, sortDir, page]);

  // debounce text/amount filters instead of requiring a submit button
  useEffect(() => {
    const t = setTimeout(() => { setPage(0); load(); }, 350);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  const handleDelete = async (id) => {
    await deleteExpense(groupId, id);
    load();
    onChanged();
  };

  return (
    <Box>
      <Typography variant="h6" sx={{ mb: 2 }}>Search Expenses</Typography>

      <Stack direction="row" spacing={1.5} sx={{ mb: 2, flexWrap: "wrap" }}>
        <TextField
          placeholder="Search description"
          value={filters.description}
          onChange={(e) => setFilters({ ...filters, description: e.target.value })}
          slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchRoundedIcon fontSize="small" /></InputAdornment> } }}
          sx={{ flex: 2, minWidth: 180 }}
        />
        <FormControl sx={{ flex: 1, minWidth: 140 }}>
          <InputLabel>Category</InputLabel>
          <Select label="Category" value={filters.categoryId} onChange={(e) => setFilters({ ...filters, categoryId: e.target.value })}>
            <MenuItem value="">All</MenuItem>
            {categories.map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
          </Select>
        </FormControl>
        <TextField label="Min" type="number" value={filters.minAmount} onChange={(e) => setFilters({ ...filters, minAmount: e.target.value })} sx={{ width: 100 }} />
        <TextField label="Max" type="number" value={filters.maxAmount} onChange={(e) => setFilters({ ...filters, maxAmount: e.target.value })} sx={{ width: 100 }} />
        <FormControl sx={{ minWidth: 130 }}>
          <InputLabel>Sort by</InputLabel>
          <Select label="Sort by" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
            <MenuItem value="createdAt">Date</MenuItem>
            <MenuItem value="amount">Amount</MenuItem>
            <MenuItem value="description">Description</MenuItem>
          </Select>
        </FormControl>
        <FormControl sx={{ minWidth: 130 }}>
          <InputLabel>Order</InputLabel>
          <Select label="Order" value={sortDir} onChange={(e) => setSortDir(e.target.value)}>
            <MenuItem value="desc">Descending</MenuItem>
            <MenuItem value="asc">Ascending</MenuItem>
          </Select>
        </FormControl>
      </Stack>

      <List disablePadding>
        {result.content.map((e, i) => (
          <Box key={e.id}>
            {i > 0 && <Divider component="li" />}
            <ListItem
              sx={{ py: 1.2, px: 0.5, borderRadius: 2, transition: "background-color 0.15s", "&:hover": { bgcolor: "var(--color-paper)" } }}
              secondaryAction={
                <Stack direction="row" spacing={0.5}>
                  <IconButton edge="end" onClick={() => setCommentsExpenseId(e.id)} sx={{ "&:hover": { color: "var(--color-brass-dark)", bgcolor: "rgba(184,137,43,0.08)" } }}>
                    <ChatBubbleOutlineRoundedIcon fontSize="small" />
                  </IconButton>
                  <IconButton edge="end" onClick={() => handleDelete(e.id)} sx={{ "&:hover": { color: "var(--color-red)", bgcolor: "var(--color-red-bg)" } }}>
                    <DeleteOutlineRoundedIcon fontSize="small" />
                  </IconButton>
                </Stack>
              }
            >
              <ListItemText
                primary={<span style={{ fontWeight: 600 }}>{e.description}</span>}
                secondary={
                  <>
                    <span style={{ fontFamily: "'IBM Plex Mono', monospace", fontWeight: 600 }}>{Number(e.amount).toFixed(2)}</span>
                    {" · "}{e.splitType}{e.categoryName ? ` · ${e.categoryName}` : ""} · paid by {e.paidByName}
                  </>
                }
              />
            </ListItem>
          </Box>
        ))}
      </List>

      <Stack direction="row" sx={{ justifyContent: "space-between", alignItems: "center", mt: 2 }}>
        <Typography variant="body2" color="text.secondary">{result.totalElements} total</Typography>
        <Pagination
          count={Math.max(result.totalPages, 1)}
          page={page + 1}
          onChange={(_e, val) => setPage(val - 1)}
          color="primary"
          size="small"
        />
      </Stack>

      <Dialog open={!!commentsExpenseId} onClose={() => setCommentsExpenseId(null)} maxWidth="xs" fullWidth>
        <DialogTitle sx={{ fontFamily: "'Fraunces', serif" }}>Comments</DialogTitle>
        <DialogContent>
          {commentsExpenseId && <ExpenseComments expenseId={commentsExpenseId} />}
        </DialogContent>
      </Dialog>
    </Box>
  );
}
