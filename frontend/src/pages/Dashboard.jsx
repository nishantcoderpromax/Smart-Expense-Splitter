import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuthStore } from "../store/authStore";
import { fetchDashboardSummary, fetchRecentActivity } from "../api/dashboardApi";
import { Card, CardContent, Typography, Box, Stack, List, ListItem, ListItemText, Divider, Skeleton } from "@mui/material";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import AmountChip from "../components/AmountChip";

const hoverCard = {
  transition: "transform 0.18s ease, box-shadow 0.18s ease",
  "&:hover": { transform: "translateY(-3px)", boxShadow: "0 10px 28px rgba(27,36,48,0.10)" },
};

export default function Dashboard() {
  const user = useAuthStore((s) => s.user);
  const [summary, setSummary] = useState(null);
  const [activity, setActivity] = useState([]);

  useEffect(() => {
    fetchDashboardSummary().then(setSummary);
    fetchRecentActivity(10).then(setActivity);
  }, []);

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>Welcome back, {user?.name}</Typography>

      {!summary ? (
        <Skeleton variant="rounded" height={90} sx={{ mb: 3, borderRadius: 3 }} />
      ) : (
        <Card sx={{ mb: 3, borderRadius: 3, ...hoverCard }}>
          <CardContent>
            <Stack direction="row" spacing={5} sx={{ flexWrap: "wrap" }}>
              <Box>
                <Typography variant="caption" color="text.secondary">You are owed</Typography>
                <Box sx={{ mt: 0.5 }}><AmountChip value={summary.totalOwedToYou} /></Box>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">You owe</Typography>
                <Box sx={{ mt: 0.5 }}><AmountChip value={-summary.totalYouOwe} /></Box>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Net</Typography>
                <Box sx={{ mt: 0.5 }}><AmountChip value={summary.netOverall} /></Box>
              </Box>
            </Stack>
          </CardContent>
        </Card>
      )}

      {summary && (
        <Card sx={{ mb: 3, borderRadius: 3, ...hoverCard }}>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 1 }}>Per Group</Typography>
            <List disablePadding>
              {summary.perGroup.map((g, i) => (
                <Box key={g.groupId}>
                  {i > 0 && <Divider component="li" />}
                  <ListItem
                    disablePadding
                    sx={{ py: 1.2, px: 0.5, borderRadius: 2, transition: "background-color 0.15s", "&:hover": { bgcolor: "var(--color-paper)" } }}
                    secondaryAction={<AmountChip value={g.netBalance} size="small" />}
                  >
                    <ListItemText
                      primary={
                        <Link to={`/groups/${g.groupId}`} style={{ fontWeight: 600, color: "var(--color-brass-dark)" }}>
                          {g.groupName}
                        </Link>
                      }
                    />
                  </ListItem>
                </Box>
              ))}
            </List>
          </CardContent>
        </Card>
      )}

      <Card sx={{ borderRadius: 3, ...hoverCard }}>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 1 }}>Recent Activity</Typography>
          {activity.length === 0 && <Typography color="text.secondary">No activity yet — go add an expense in a group.</Typography>}
          <List disablePadding>
            {activity.map((a, i) => (
              <ListItem key={i} disablePadding sx={{ py: 1, gap: 1.5 }}>
                <ReceiptLongRoundedIcon sx={{ color: "var(--color-brass)", fontSize: 20 }} />
                <ListItemText
                  primary={
                    <>
                      <Link to={`/groups/${a.groupId}`} style={{ fontWeight: 600 }}>{a.groupName}</Link>
                      {": "}{a.paidByName} paid <span style={{ fontFamily: "'IBM Plex Mono', monospace", fontWeight: 600 }}>{Number(a.amount).toFixed(2)}</span> for "{a.description}"
                    </>
                  }
                />
              </ListItem>
            ))}
          </List>
        </CardContent>
      </Card>
    </Box>
  );
}