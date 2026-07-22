import axiosClient from "./axiosClient";

export const fetchProfile = () => axiosClient.get("/users/me").then((r) => r.data);
export const updateProfile = (payload) => axiosClient.put("/users/me", payload).then((r) => r.data);
export const searchUsers = (query) =>
  axiosClient.get("/users/search", { params: { query } }).then((r) => r.data);