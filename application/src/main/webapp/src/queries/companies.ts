import axios, { AxiosError } from "axios";
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from "react-query";

import { CompanyDto } from "api/models";
import { usePollingContext } from "shared/context";

export const useCompaniesQuery = (
  projectName: string | null
): UseQueryResult<CompanyDto[], AxiosError> => {
  const result = useQuery<CompanyDto[], AxiosError>({
    queryKey: ["companies", projectName],
    queryFn: async () => {
      const url = `/projects/${projectName}/companies`;
      return (await axios.get<CompanyDto[]>(url)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
    enabled: !!projectName,
  });
  return result;
};

export const useCreateCompanyMutation = (
  projectName: string | null,
  onSuccess?: (company: CompanyDto) => void
): UseMutationResult<CompanyDto, AxiosError, CompanyDto> => {
  const queryClient = useQueryClient();
  return useMutation<CompanyDto, AxiosError, CompanyDto>(
    async (company) => {
      const url = `/projects/${projectName}/companies`;
      return (await axios.post<CompanyDto>(url, company)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries(["companies", projectName]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useUpdateCompanyMutation = (
  projectName: string | null,
  onSuccess?: (project: CompanyDto) => void
): UseMutationResult<CompanyDto, AxiosError, CompanyDto> => {
  const queryClient = useQueryClient();
  return useMutation<CompanyDto, AxiosError, CompanyDto>(
    async (company) => {
      const url = `/projects/${projectName}/companies/${company.ruc}`;
      return (await axios.put<CompanyDto>(url, company)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries(["companies", projectName]);
        queryClient.invalidateQueries(["logo", projectName]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useDeleteCompanyMutation = (
  projectName: string | null,
  onSuccess?: () => void
): UseMutationResult<void, AxiosError, CompanyDto, unknown> => {
  const queryClient = useQueryClient();

  return useMutation<void, AxiosError, CompanyDto>(
    async (company: CompanyDto) => {
      const url = `/projects/${projectName}/companies/${company.ruc}`;
      await axios.delete<void>(url);
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(["companies", projectName]);
        queryClient.invalidateQueries(["logo", projectName]);
        onSuccess && onSuccess();
      },
    }
  );
};

export const useCompanyLogoQuery = (
  projectName: string | null,
  ruc: string | null
): UseQueryResult<string, AxiosError> => {
  const result = useQuery<string, AxiosError>({
    queryKey: ["logo", projectName, ruc],
    queryFn: async () => {
      const url = `/projects/${projectName}/companies/${ruc}/logo`;
      return (await axios.get<string>(url)).data;
    },
    refetchInterval: false,
    enabled: !!projectName,
  });
  return result;
};
