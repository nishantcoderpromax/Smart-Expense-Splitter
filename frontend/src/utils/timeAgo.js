/** Formats an ISO timestamp as "just now" / "5 min ago" / "3 hr ago" / "2 days ago". */
export function timeAgo(isoString) {
  const diffMs = Date.now() - new Date(isoString).getTime();
  const diffSec = Math.floor(diffMs / 1000);
 
  if (diffSec < 30) return "just now";
  if (diffSec < 60) return `${diffSec} sec ago`;
 
  const diffMin = Math.floor(diffSec / 60);
  if (diffMin < 60) return `${diffMin} min ago`;
 
  const diffHr = Math.floor(diffMin / 60);
  if (diffHr < 24) return `${diffHr} hr ago`;
 
  const diffDays = Math.floor(diffHr / 24);
  if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? "s" : ""} ago`;
 
  return new Date(isoString).toLocaleDateString();
}