import { Chip } from "@mui/material";
import ArrowUpwardRoundedIcon from "@mui/icons-material/ArrowUpwardRounded";
import ArrowDownwardRoundedIcon from "@mui/icons-material/ArrowDownwardRounded";
import RemoveRoundedIcon from "@mui/icons-material/RemoveRounded";

/** Color-coded, icon-led money chip: green+up = owed to you, red+down = you owe. */
export default function AmountChip({ value, size = "small" }) {
  const num = Number(value);
  const positive = num > 0;
  const negative = num < 0;

  const icon = positive
    ? <ArrowUpwardRoundedIcon style={{ fontSize: 16 }} />
    : negative
    ? <ArrowDownwardRoundedIcon style={{ fontSize: 16 }} />
    : <RemoveRoundedIcon style={{ fontSize: 16 }} />;

  return (
    <Chip
      size={size}
      icon={icon}
      label={Math.abs(num).toFixed(2)}
      sx={{
        fontFamily: "'IBM Plex Mono', monospace",
        fontWeight: 700,
        color: positive ? "var(--color-green)" : negative ? "var(--color-red)" : "var(--color-ink-soft)",
        bgcolor: positive ? "var(--color-green-bg)" : negative ? "var(--color-red-bg)" : "var(--color-rule)",
        "& .MuiChip-icon": { color: "inherit" },
        "&:hover": { transform: "scale(1.04)" },
      }}
    />
  );
}
