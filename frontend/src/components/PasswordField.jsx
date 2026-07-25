import { useState } from "react";
import { TextField, IconButton, Box, Typography, LinearProgress, Stack } from "@mui/material";
import VisibilityRoundedIcon from "@mui/icons-material/VisibilityRounded";
import VisibilityOffRoundedIcon from "@mui/icons-material/VisibilityOffRounded";
import CheckCircleRoundedIcon from "@mui/icons-material/CheckCircleRounded";
import RadioButtonUncheckedRoundedIcon from "@mui/icons-material/RadioButtonUncheckedRounded";
import { analyzePassword } from "../utils/passwordStrength";

/**
 * showStrength: turns on the strength bar + rule checklist below the field —
 * use this for "create a new password" fields (Register, Reset, Change password),
 * but leave it off for a plain "enter your existing password" field (Login,
 * current-password confirmation) where a strength meter doesn't make sense.
 */
export default function PasswordField({ label, value, onChange, showStrength = false, ...props }) {
  const [visible, setVisible] = useState(false);
  const analysis = showStrength ? analyzePassword(value || "") : null;

  return (
    <Box>
      <TextField
        label={label}
        type={visible ? "text" : "password"}
        value={value}
        onChange={onChange}
        fullWidth
        slotProps={{
          input: {
            endAdornment: (
              <IconButton onClick={() => setVisible((v) => !v)} edge="end" size="small" tabIndex={-1}>
                {visible ? <VisibilityOffRoundedIcon fontSize="small" /> : <VisibilityRoundedIcon fontSize="small" />}
              </IconButton>
            ),
          },
        }}
        {...props}
      />

      {showStrength && value && (
        <Box sx={{ mt: 1 }}>
          <Stack direction="row" sx={{ alignItems: "center", justifyContent: "space-between", mb: 0.5 }}>
            <LinearProgress
              variant="determinate"
              value={(analysis.score / analysis.maxScore) * 100}
              sx={{
                flex: 1, mr: 1.5, height: 6, borderRadius: 3,
                bgcolor: "var(--color-rule)",
                "& .MuiLinearProgress-bar": { bgcolor: analysis.color, borderRadius: 3 },
              }}
            />
            <Typography variant="caption" sx={{ fontWeight: 700, color: analysis.color, minWidth: 48 }}>
              {analysis.label}
            </Typography>
          </Stack>

          <Stack sx={{ gap: 0.25 }}>
            {analysis.rules.map((rule) => (
              <Stack key={rule.label} direction="row" spacing={0.75} sx={{ alignItems: "center" }}>
                {rule.met
                  ? <CheckCircleRoundedIcon sx={{ fontSize: 14, color: "var(--color-green)" }} />
                  : <RadioButtonUncheckedRoundedIcon sx={{ fontSize: 14, color: "var(--color-ink-soft)" }} />}
                <Typography variant="caption" sx={{ color: rule.met ? "var(--color-ink)" : "var(--color-ink-soft)" }}>
                  {rule.label}
                </Typography>
              </Stack>
            ))}
          </Stack>
        </Box>
      )}
    </Box>
  );
}