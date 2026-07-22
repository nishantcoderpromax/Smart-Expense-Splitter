import axiosClient from "./axiosClient";
 
export const fetchMyGroups = () => axiosClient.get("/groups").then((r) => r.data);
export const fetchGroup = (groupId) => axiosClient.get(`/groups/${groupId}`).then((r) => r.data);
export const createGroup = (payload) => axiosClient.post("/groups", payload).then((r) => r.data);
export const addMember = (groupId, email) =>
  axiosClient.post(`/groups/${groupId}/members`, { email }).then((r) => r.data);
export const removeMember = (groupId, userId) => axiosClient.delete(`/groups/${groupId}/members/${userId}`);
 
export const generateInvite = (groupId) => axiosClient.post(`/groups/${groupId}/invite`).then((r) => r.data);
export const fetchInvite = (groupId) =>
  axiosClient.get(`/groups/${groupId}/invite`).then((r) => (r.status === 204 ? null : r.data));
export const revokeInvite = (groupId) => axiosClient.delete(`/groups/${groupId}/invite`);
export const joinGroupViaInvite = (token) => axiosClient.post(`/groups/join/${token}`).then((r) => r.data);