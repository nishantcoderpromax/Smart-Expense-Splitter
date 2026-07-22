import axiosClient from "./axiosClient";

export const fetchMonthlySpend = (groupId) =>
  axiosClient.get(`/groups/${groupId}/analytics/monthly`).then((r) => r.data);

export const fetchCategorySpend = (groupId) =>
  axiosClient.get(`/groups/${groupId}/analytics/category`).then((r) => r.data);