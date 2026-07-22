import axios from "axios";
import { useAuthStore } from "../store/authStore";
import { API_BASE_URL } from "./apiConfig";
 
const axiosClient = axios.create({
  baseURL: API_BASE_URL,
});
 
// Attach access token to every request
axiosClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
 
// Shared across all callers so N simultaneous 401s trigger exactly ONE refresh
// call instead of a race where each one rotates (and invalidates) the token
// before the others get to use it.
let refreshPromise = null;
 
function refreshAccessToken() {
  if (!refreshPromise) {
    const refreshToken = useAuthStore.getState().refreshToken;
    refreshPromise = axios
      .post(`${API_BASE_URL}`, { refreshToken })
      .then(({ data }) => {
        useAuthStore.getState().setAuth(data);
        return data;
      })
      .finally(() => {
        refreshPromise = null; // next expiry gets a fresh refresh call
      });
  }
  return refreshPromise;
}
 
// On 401, refresh (once, shared) then retry the original request
axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      try {
        const data = await refreshAccessToken();
        original.headers.Authorization = `Bearer ${data.accessToken}`;
        return axiosClient(original);
      } catch (refreshError) {
        useAuthStore.getState().clearAuth();
        // guard against every failed request separately forcing a navigation
        if (window.location.pathname !== "/login") {
          window.location.href = "/login";
        }
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);
 
export default axiosClient;