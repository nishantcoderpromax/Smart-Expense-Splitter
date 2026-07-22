import { useRef, useState } from "react";
import { createWorker } from "tesseract.js";
import { parseReceiptText } from "../utils/parseReceiptText";
import { Box, Button, LinearProgress, Typography, Alert } from "@mui/material";
import DocumentScannerRoundedIcon from "@mui/icons-material/DocumentScannerRounded";

/** onExtracted({ amount, description }) — caller decides what to do with the guess;
 *  this component never submits anything itself, only ever pre-fills a form. */
export default function ReceiptScanner({ onExtracted }) {
  const fileInputRef = useRef(null);
  const [scanning, setScanning] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState("");

  const handleFileSelect = async (e) => {
    const file = e.target.files?.[0];
    e.target.value = ""; // allow re-selecting the same file later
    if (!file) return;

    setScanning(true);
    setProgress(0);
    setError("");

    try {
      const worker = await createWorker("eng", 1, {
        logger: (m) => {
          if (m.status === "recognizing text") {
            setProgress(Math.round(m.progress * 100));
          }
        },
      });

      const { data } = await worker.recognize(file);
      await worker.terminate();

      const { amount, description } = parseReceiptText(data.text);

      if (amount == null) {
        setError("Couldn't find a total on this receipt — try a clearer photo, or enter it manually below.");
      }
      onExtracted({ amount, description });
    } catch (err) {
      setError("Something went wrong reading this image. Try a clearer, well-lit photo.");
    } finally {
      setScanning(false);
    }
  };

  return (
    <Box sx={{ mb: 2 }}>
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        capture="environment"
        style={{ display: "none" }}
        onChange={handleFileSelect}
      />

      <Button
        variant="outlined"
        startIcon={<DocumentScannerRoundedIcon />}
        onClick={() => fileInputRef.current?.click()}
        disabled={scanning}
        sx={{ borderColor: "var(--color-rule)", color: "var(--color-ink)", "&:hover": { borderColor: "var(--color-brass)" } }}
      >
        {scanning ? `Reading receipt... ${progress}%` : "Scan Receipt"}
      </Button>

      {scanning && <LinearProgress variant="determinate" value={progress} sx={{ mt: 1, borderRadius: 1 }} />}
      {error && <Alert severity="warning" sx={{ mt: 1 }}>{error}</Alert>}

      <Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 0.5 }}>
        Runs entirely in your browser — the image never leaves your device. Always double-check the extracted amount.
      </Typography>
    </Box>
  );
}
