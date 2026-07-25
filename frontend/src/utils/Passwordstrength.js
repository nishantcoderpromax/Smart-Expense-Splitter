/**
 * Simple heuristic scoring — not cryptographic, just enough to give people
 * useful feedback. Each rule met adds one point; score maps to a label/color.
 */
export function analyzePassword(password) {
  const rules = [
    { label: "At least 8 characters", met: password.length >= 8 },
    { label: "One uppercase letter", met: /[A-Z]/.test(password) },
    { label: "One lowercase letter", met: /[a-z]/.test(password) },
    { label: "One number", met: /[0-9]/.test(password) },
    { label: "One special character", met: /[^A-Za-z0-9]/.test(password) },
  ];

  const score = rules.filter((r) => r.met).length;

  let label = "Weak";
  let color = "var(--color-red)";
  if (score >= 5) {
    label = "Strong";
    color = "var(--color-green)";
  } else if (score >= 3) {
    label = "Medium";
    color = "var(--color-brass)";
  }

  return { rules, score, maxScore: rules.length, label, color };
}