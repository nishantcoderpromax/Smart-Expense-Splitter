import axiosClient from "./axiosClient";
 
export const fetchExpenses = (groupId) =>
  axiosClient.get(`/groups/${groupId}/expenses`).then((r) => r.data);
 
export const addExpense = (groupId, payload) =>
  axiosClient.post(`/groups/${groupId}/expenses`, payload).then((r) => r.data);
 
export const deleteExpense = (groupId, expenseId) =>
  axiosClient.delete(`/groups/${groupId}/expenses/${expenseId}`);
export const searchExpenses = (groupId, params) =>
  axiosClient
    .get(`/groups/${groupId}/expenses/search`, { params })
    .then((r) => r.data);