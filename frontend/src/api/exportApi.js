import axiosClient from "./axiosClient";

// Downloads the file directly using a Blob response, then triggers a save via an <a> click.
async function downloadFile(url, filename) {
  const response = await axiosClient.get(url, { responseType: "blob" });
  const blobUrl = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement("a");
  link.href = blobUrl;
  link.download = filename;
  link.click();
  window.URL.revokeObjectURL(blobUrl);
}

export const downloadCsv = (groupId) => downloadFile(`/groups/${groupId}/export/csv`, "expenses.csv");
export const downloadPdf = (groupId) => downloadFile(`/groups/${groupId}/export/pdf`, "expenses.pdf");
