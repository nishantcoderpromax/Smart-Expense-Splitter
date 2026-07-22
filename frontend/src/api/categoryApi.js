import axiosClient from "./axiosClient";

export const fetchCategories = () => axiosClient.get("/categories").then((r) => r.data);
