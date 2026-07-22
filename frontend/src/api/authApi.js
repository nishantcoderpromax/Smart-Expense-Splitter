import axiosClient from "./axiosClient";

export const registerUser = (payload) => axiosClient.post("/auth/register", payload).then((r) => r.data);
export const loginUser = (payload) => axiosClient.post("/auth/login", payload).then((r) => r.data);
export const logoutUser = (refreshToken) => axiosClient.post("/auth/logout", { refreshToken });

export const verifyEmail = (token) => axiosClient.post("/auth/verify-email", { token });
export const resendVerification = () => axiosClient.post("/auth/resend-verification");
export const forgotPassword = (email) => axiosClient.post("/auth/forgot-password", { email });
export const resetPassword = (token, newPassword) => axiosClient.post("/auth/reset-password", { token, newPassword });