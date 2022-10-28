import { AxiosError } from "axios";
import * as prettier from "prettier/standalone";
import * as prettierXmlPlugin from "@prettier/plugin-xml";

export const getBaseApiUrl = (): string => {
  return (window as any)["UBLHUB_API_URL"] || "/api";
};

// Axios error

export const getAxiosErrorMessage = (axiosError: AxiosError) => {
  if (axiosError.response?.data.message) {
    return axiosError.response?.data.message;
  }
  return axiosError.message;
};

export const formatNumber = (value: number, fractionDigits = 2) => {
  return value.toLocaleString("en", {
    style: "decimal",
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  });
};

// Formik

export const getValidatedFromError = (
  error: any
): "success" | "warning" | "error" | "default" => {
  return error ? "error" : "default";
};

export const getValidatedFromErrorTouched = (
  error: any,
  touched: boolean | undefined
): "success" | "warning" | "error" | "default" => {
  return error && touched ? "error" : "default";
};

// XML

export const prettifyXML = (data: string) => {
  return prettier.format(data, {
    parser: "xml",
    plugins: [prettierXmlPlugin],
    xmlWhitespaceSensitivity: "ignore",
  } as any);
};
