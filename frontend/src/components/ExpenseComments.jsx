import { useEffect, useState } from "react";
import { fetchComments, addComment, deleteComment } from "../api/commentApi";
import { useTopicSocket } from "../hooks/useTopicSocket";
import { useAuthStore } from "../store/authStore";
import { timeAgo } from "../utils/timeAgo";
import {
  Box, Avatar, Typography, TextField, IconButton, Stack, Divider, CircularProgress,
} from "@mui/material";
import SendRoundedIcon from "@mui/icons-material/SendRounded";
import DeleteOutlineRoundedIcon from "@mui/icons-material/DeleteOutlineRounded";

export default function ExpenseComments({ expenseId }) {
  const [comments, setComments] = useState(null); // null = still loading
  const [text, setText] = useState("");
  const [sending, setSending] = useState(false);
  const currentUser = useAuthStore((s) => s.user);

  const load = () => fetchComments(expenseId).then(setComments);

  useEffect(() => { load(); }, [expenseId]);

  // live updates: anyone else commenting on this same expense refreshes the thread
  useTopicSocket(expenseId ? `/topic/expenses/${expenseId}` : null, () => load());

  const handleSend = async () => {
    if (!text.trim()) return;
    setSending(true);
    try {
      await addComment(expenseId, text.trim());
      setText("");
      load();
    } finally {
      setSending(false);
    }
  };

  const handleDelete = async (commentId) => {
    await deleteComment(expenseId, commentId);
    load();
  };

  if (comments === null) {
    return <Box sx={{ display: "flex", justifyContent: "center", py: 2 }}><CircularProgress size={22} /></Box>;
  }

  return (
    <Box>
      <Stack spacing={1.5} sx={{ maxHeight: 280, overflowY: "auto", mb: 2 }}>
        {comments.length === 0 && (
          <Typography color="text.secondary" sx={{ fontSize: "0.9rem" }}>
            No comments yet — start the discussion.
          </Typography>
        )}
        {comments.map((c) => (
          <Stack direction="row" spacing={1.5} key={c.id} sx={{ alignItems: "flex-start" }}>
            <Avatar sx={{ width: 30, height: 30, fontSize: "0.8rem", bgcolor: "var(--color-brass)" }}>
              {c.userName?.[0]?.toUpperCase()}
            </Avatar>
            <Box sx={{ flex: 1 }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: "baseline" }}>
                <Typography sx={{ fontWeight: 600, fontSize: "0.88rem" }}>{c.userName}</Typography>
                <Typography sx={{ fontSize: "0.75rem", color: "var(--color-ink-soft)" }}>{timeAgo(c.createdAt)}</Typography>
              </Stack>
              <Typography sx={{ fontSize: "0.9rem" }}>{c.content}</Typography>
            </Box>
            {c.userId === currentUser?.id && (
              <IconButton size="small" onClick={() => handleDelete(c.id)} sx={{ "&:hover": { color: "var(--color-red)" } }}>
                <DeleteOutlineRoundedIcon fontSize="small" />
              </IconButton>
            )}
          </Stack>
        ))}
      </Stack>

      <Divider sx={{ mb: 1.5 }} />

      <Stack direction="row" spacing={1}>
        <TextField
          size="small" fullWidth placeholder="Write a comment..."
          value={text} onChange={(e) => setText(e.target.value)}
          onKeyDown={(e) => { if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); handleSend(); } }}
        />
        <IconButton onClick={handleSend} disabled={sending || !text.trim()} sx={{ color: "var(--color-brass)" }}>
          <SendRoundedIcon />
        </IconButton>
      </Stack>
    </Box>
  );
}
