import axiosClient from "./axiosClient";

export const fetchRecurringExpenses = (groupId) =>
  axiosClient.get(`/groups/${groupId}/recurring-expenses`).then((r) => r.data);

export const createRecurringExpense = (groupId, payload) =>
  axiosClient.post(`/groups/${groupId}/recurring-expenses`, payload).then((r) => r.data);

export const setRecurringExpenseActive = (groupId, id, active) =>
  axiosClient.patch(`/groups/${groupId}/recurring-expenses/${id}/active`, null, { params: { active } });

export const deleteRecurringExpense = (groupId, id) =>
  axiosClient.delete(`/groups/${groupId}/recurring-expenses/${id}`);

export const runRecurringExpenseNow = (groupId, id) =>
  axiosClient.post(`/groups/${groupId}/recurring-expenses/${id}/run-now`).then((r) => r.data);
