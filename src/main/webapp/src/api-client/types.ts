import { AxiosError } from "axios";

export type ApiClientError = AxiosError<{ message: string }>;
