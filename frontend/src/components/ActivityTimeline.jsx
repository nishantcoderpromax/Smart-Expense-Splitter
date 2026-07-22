import { useEffect, useState } from "react";
import { fetchActivityLog } from "../api/activityLogApi";
import { timeAgo } from "../utils/timeAgo";
import {
  Timeline, TimelineItem, TimelineSeparator, TimelineDot, TimelineConnector, TimelineContent, TimelineOppositeContent,
} from "@mui/lab";
import { Typography, Button, Box } from "@mui/material";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import DeleteOutlineRoundedIcon from "@mui/icons-material/DeleteOutlineRounded";
import PersonAddRoundedIcon from "@mui/icons-material/PersonAddRounded";
import PersonRemoveRoundedIcon from "@mui/icons-material/PersonRemoveRounded";
import PaidRoundedIcon from "@mui/icons-material/PaidRounded";

const ICONS = {
  EXPENSE_ADDED: { icon: <ReceiptLongRoundedIcon fontSize="small" />, color: "var(--color-brass)" },
  EXPENSE_DELETED: { icon: <DeleteOutlineRoundedIcon fontSize="small" />, color: "var(--color-red)" },
  MEMBER_ADDED: { icon: <PersonAddRoundedIcon fontSize="small" />, color: "var(--color-green)" },
  MEMBER_REMOVED: { icon: <PersonRemoveRoundedIcon fontSize="small" />, color: "var(--color-red)" },
  SETTLEMENT_RECORDED: { icon: <PaidRoundedIcon fontSize="small" />, color: "var(--color-green)" },
};

export default function ActivityTimeline({ groupId, refreshKey }) {
  const [entries, setEntries] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [, forceTick] = useState(0); // re-renders periodically so "2 min ago" stays fresh

  const load = (nextPage = 0, append = false) => {
    fetchActivityLog(groupId, nextPage, 15).then((res) => {
      setEntries((prev) => (append ? [...prev, ...res.content] : res.content));
      setTotalPages(res.totalPages);
      setPage(nextPage);
    });
  };

  useEffect(() => { load(0, false); }, [groupId, refreshKey]);

  // keep relative timestamps ("2 min ago") accurate without needing new data
  useEffect(() => {
    const t = setInterval(() => forceTick((n) => n + 1), 30000);
    return () => clearInterval(t);
  }, []);

  if (entries.length === 0) {
    return <Typography color="text.secondary">No activity yet — actions in this group will show up here.</Typography>;
  }

  return (
    <Box>
      <Timeline sx={{ p: 0, m: 0, "& .MuiTimelineItem-root:before": { flex: 0, padding: 0 } }}>
        {entries.map((e, i) => {
          const meta = ICONS[e.type] || { icon: <ReceiptLongRoundedIcon fontSize="small" />, color: "var(--color-ink-soft)" };
          return (
            <TimelineItem key={e.id}>
              <TimelineOppositeContent sx={{ flex: 0, minWidth: 90, color: "var(--color-ink-soft)", fontSize: "0.8rem", pt: 1.2 }}>
                {timeAgo(e.createdAt)}
              </TimelineOppositeContent>
              <TimelineSeparator>
                <TimelineDot sx={{ bgcolor: meta.color, color: "#fff", boxShadow: "none" }}>
                  {meta.icon}
                </TimelineDot>
                {i < entries.length - 1 && <TimelineConnector sx={{ bgcolor: "var(--color-rule)" }} />}
              </TimelineSeparator>
              <TimelineContent sx={{ pb: 2.5 }}>
                <Typography sx={{ fontSize: "0.92rem" }}>{e.description}</Typography>
              </TimelineContent>
            </TimelineItem>
          );
        })}
      </Timeline>

      {page + 1 < totalPages && (
        <Button size="small" onClick={() => load(page + 1, true)} sx={{ ml: 2 }}>
          Load more
        </Button>
      )}
    </Box>
  );
}