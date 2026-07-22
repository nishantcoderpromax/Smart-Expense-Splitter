import axiosClient from "./axiosClient";
 
export const fetchBalances = (groupId) =>
  axiosClient.get(`/groups/${groupId}/balances`).then((r) => r.data);
 
export const fetchSettlements = (groupId) =>
  axiosClient.get(`/groups/${groupId}/settlements`).then((r) => r.data);
 
export const recordSettlement = (groupId, toUserId, amount) =>
  axiosClient.post(`/groups/${groupId}/settlements`, { toUserId, amount }).then((r) => r.data);
 
export const fetchSettlementHistory = (groupId) =>
  axiosClient.get(`/groups/${groupId}/settlements/history`).then((r) => r.data);