import axiosClient from "./axiosClient";

export const fetchComments = (expenseId) =>
  axiosClient.get(`/expenses/${expenseId}/comments`).then((r) => r.data);

export const addComment = (expenseId, content) =>
  axiosClient.post(`/expenses/${expenseId}/comments`, { content }).then((r) => r.data);

export const deleteComment = (expenseId, commentId) =>
  axiosClient.delete(`/expenses/${expenseId}/comments/${commentId}`);