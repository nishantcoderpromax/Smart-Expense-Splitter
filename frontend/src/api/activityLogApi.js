import axiosClient from "./axiosClient";
 
export const fetchActivityLog = (groupId, page = 0, size = 15) =>
  axiosClient.get(`/groups/${groupId}/activity-log`, { params: { page, size } }).then((r) => r.data);