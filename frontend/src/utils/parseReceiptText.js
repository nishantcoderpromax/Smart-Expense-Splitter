/**
 * Receipts are messy OCR text — this is a heuristic parser, not a guarantee.
 * The caller must always let the person review/edit the result before
 * submitting; never treat this as ground truth.
 */
export function parseReceiptText(rawText) {
  const lines = rawText
    .split("\n")
    .map((l) => l.trim())
    .filter(Boolean);

  return {
    amount: extractTotal(lines, rawText),
    description: extractMerchant(lines),
  };
}

const TOTAL_KEYWORDS = /\b(grand total|total due|amount due|balance due|total)\b/i;
const MONEY_PATTERN = /\d{1,3}(?:[.,]\d{3})*[.,]\d{2}\b/g;

function extractTotal(lines, rawText) {
  // Prefer a line that explicitly says "total" — most reliable signal on a receipt.
  for (const line of lines) {
    if (TOTAL_KEYWORDS.test(line)) {
      const matches = line.match(MONEY_PATTERN);
      if (matches && matches.length > 0) {
        return normalizeAmount(matches[matches.length - 1]);
      }
    }
  }

  // Fallback: the largest money-looking number anywhere in the receipt —
  // subtotal/tax/total are usually all present, and total is usually the biggest.
  const allMatches = rawText.match(MONEY_PATTERN);
  if (allMatches && allMatches.length > 0) {
    const amounts = allMatches.map(normalizeAmount).filter((n) => !isNaN(n));
    if (amounts.length > 0) return Math.max(...amounts);
  }

  return null;
}

function normalizeAmount(str) {
  // Handles both "1,234.56" and "1.234,56" style formatting by stripping
  // thousands separators and normalizing the decimal separator to a dot.
  const cleaned = str.replace(/[^\d.,]/g, "");
  const normalized = cleaned.includes(",") && cleaned.lastIndexOf(",") > cleaned.lastIndexOf(".")
    ? cleaned.replace(/\./g, "").replace(",", ".")
    : cleaned.replace(/,/g, "");
  return parseFloat(normalized);
}

function extractMerchant(lines) {
  // The store/merchant name is almost always the first substantial line —
  // skip anything that's just numbers, symbols, or too short to be a name.
  for (const line of lines.slice(0, 5)) {
    if (line.length >= 3 && /[a-zA-Z]/.test(line) && !/^\d+$/.test(line)) {
      return line.length > 60 ? line.slice(0, 60) : line;
    }
  }
  return "Scanned receipt";
}
