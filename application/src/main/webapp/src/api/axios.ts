import axios from "axios";

export const API_BASE_URL = process.env.PUBLIC_URL + "/api";

export const iniAxios = () => {
  axios.defaults.baseURL = API_BASE_URL;
};
