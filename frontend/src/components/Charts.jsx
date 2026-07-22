import { useEffect, useState } from "react";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from "recharts";
import { fetchMonthlySpend, fetchCategorySpend } from "../api/analyticsApi";
import { Box, Typography } from "@mui/material";

const COLORS = ["#B8892B", "#1F7A5C", "#B23A2E", "#5B6472", "#96700F", "#3C8C6E", "#C9A85C", "#8C4A3E"];

export default function Charts({ groupId, refreshKey }) {
  const [monthly, setMonthly] = useState([]);
  const [byCategory, setByCategory] = useState([]);

  useEffect(() => {
    fetchMonthlySpend(groupId).then(setMonthly);
    fetchCategorySpend(groupId).then(setByCategory);
  }, [groupId, refreshKey]);

  return (
    <Box>
      <Typography variant="h6" sx={{ mb: 1 }}>Spending by Month</Typography>
      {monthly.length === 0 ? (
        <Typography color="text.secondary" sx={{ mb: 3 }}>No expenses yet.</Typography>
      ) : (
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={monthly}>
            <XAxis dataKey="month" tick={{ fontSize: 12, fill: "var(--color-ink-soft)" }} />
            <YAxis tick={{ fontSize: 12, fill: "var(--color-ink-soft)" }} />
            <Tooltip contentStyle={{ borderRadius: 8, border: "1px solid var(--color-rule)" }} />
            <Bar dataKey="total" fill="var(--color-brass)" radius={[6, 6, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      )}

      <Typography variant="h6" sx={{ mt: 3, mb: 1 }}>Spending by Category</Typography>
      {byCategory.length === 0 ? (
        <Typography color="text.secondary">No expenses yet.</Typography>
      ) : (
        <ResponsiveContainer width="100%" height={260}>
          <PieChart>
            <Pie data={byCategory} dataKey="total" nameKey="categoryName" outerRadius={90} label>
              {byCategory.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
            </Pie>
            <Tooltip contentStyle={{ borderRadius: 8, border: "1px solid var(--color-rule)" }} />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      )}
    </Box>
  );
}


