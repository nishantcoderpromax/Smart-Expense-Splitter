import axiosClient from "./axiosClient";
 
export const fetchDashboardSummary = () => axiosClient.get("/dashboard/summary").then((r) => r.data);
export const fetchRecentActivity = (limit = 10) =>
  axiosClient.get(`/dashboard/activity?limit=${limit}`).then((r) => r.data);