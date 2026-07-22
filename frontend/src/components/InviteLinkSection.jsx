import { useEffect, useState } from "react";
import { fetchInvite, generateInvite, revokeInvite } from "../api/groupApi";
import { Box, Typography, Button, TextField, IconButton, Stack, Snackbar } from "@mui/material";
import LinkRoundedIcon from "@mui/icons-material/LinkRounded";
import ContentCopyRoundedIcon from "@mui/icons-material/ContentCopyRounded";
import DeleteOutlineRoundedIcon from "@mui/icons-material/DeleteOutlineRounded";
import RefreshRoundedIcon from "@mui/icons-material/RefreshRounded";

/** isAdmin gates generate/revoke — any member can still see and copy an existing link. */
export default function InviteLinkSection({ groupId, isAdmin }) {
  const [invite, setInvite] = useState(undefined); // undefined = loading, null = none active
  const [copied, setCopied] = useState(false);

  const load = () => fetchInvite(groupId).then(setInvite);

  useEffect(() => { load(); }, [groupId]);

  const handleGenerate = async () => {
    await generateInvite(groupId);
    load();
  };

  const handleRevoke = async () => {
    await revokeInvite(groupId);
    load();
  };

  const handleCopy = async () => {
    await navigator.clipboard.writeText(invite.inviteUrl);
    setCopied(true);
  };

  if (invite === undefined) return null;

  return (
    <Box sx={{ mt: 2, pt: 2, borderTop: "1px dashed var(--color-rule)" }}>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 1, display: "flex", alignItems: "center", gap: 0.5 }}>
        <LinkRoundedIcon fontSize="small" /> Invite link
      </Typography>

      {invite ? (
        <Stack direction="row" spacing={1} sx={{ alignItems: "center", flexWrap: "wrap" }}>
          <TextField
            size="small" value={invite.inviteUrl}
            sx={{ flex: 1, minWidth: 220 }}
            slotProps={{ htmlInput: { readOnly: true } }}
          />
          <IconButton onClick={handleCopy} sx={{ color: "var(--color-brass)" }}>
            <ContentCopyRoundedIcon fontSize="small" />
          </IconButton>
          {isAdmin && (
            <>
              <IconButton onClick={handleGenerate} title="Regenerate" sx={{ "&:hover": { color: "var(--color-brass-dark)" } }}>
                <RefreshRoundedIcon fontSize="small" />
              </IconButton>
              <IconButton onClick={handleRevoke} title="Revoke" sx={{ "&:hover": { color: "var(--color-red)" } }}>
                <DeleteOutlineRoundedIcon fontSize="small" />
              </IconButton>
            </>
          )}
          <Typography variant="caption" color="text.secondary" sx={{ width: "100%" }}>
            Expires {new Date(invite.expiresAt).toLocaleDateString()} — anyone with this link can join.
          </Typography>
        </Stack>
      ) : (
        isAdmin ? (
          <Button size="small" variant="outlined" onClick={handleGenerate} startIcon={<LinkRoundedIcon />}>
            Generate Invite Link
          </Button>
        ) : (
          <Typography variant="body2" color="text.secondary">No active invite link — ask a group admin to create one.</Typography>
        )
      )}

      <Snackbar open={copied} autoHideDuration={2000} onClose={() => setCopied(false)} message="Link copied" />
    </Box>
  );
}