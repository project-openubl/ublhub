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
  projectId: string | null
): UseQueryResult<CompanyDto[], AxiosError> => {
  const result = useQuery<CompanyDto[], AxiosError>({
    queryKey: ["companies", projectId],
    queryFn: async () => {
      const url = `/projects/${projectId}/companies`;
      return (await axios.get<CompanyDto[]>(url)).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
    enabled: !!projectId,
  });
  return result;
};

export const useCreateCompanyMutation = (
  projectId: string | null,
  onSuccess?: (company: CompanyDto) => void
): UseMutationResult<CompanyDto, AxiosError, CompanyDto> => {
  const queryClient = useQueryClient();
  return useMutation<CompanyDto, AxiosError, CompanyDto>(
    async (company) => {
      const url = `/projects/${projectId}/companies`;
      return (await axios.post<CompanyDto>(url, company)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries(["companies", projectId]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useUpdateCompanyMutation = (
  projectId: string | null,
  onSuccess?: (project: CompanyDto) => void
): UseMutationResult<CompanyDto, AxiosError, CompanyDto> => {
  const queryClient = useQueryClient();
  return useMutation<CompanyDto, AxiosError, CompanyDto>(
    async (company) => {
      const url = `/projects/${projectId}/companies/${company.ruc}`;
      return (await axios.put<CompanyDto>(url, company)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries(["companies", projectId]);
        queryClient.invalidateQueries(["logo", projectId]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useDeleteCompanyMutation = (
  projectId: string | null,
  onSuccess?: () => void
): UseMutationResult<void, AxiosError, CompanyDto, unknown> => {
  const queryClient = useQueryClient();

  return useMutation<void, AxiosError, CompanyDto>(
    async (company: CompanyDto) => {
      const url = `/projects/${projectId}/companies/${company.ruc}`;
      await axios.delete<void>(url);
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(["companies", projectId]);
        queryClient.invalidateQueries(["logo", projectId]);
        onSuccess && onSuccess();
      },
    }
  );
};

export const useCompanyLogoQuery = (
  projectId: string | null,
  companyId: string | null
): UseQueryResult<string, AxiosError> => {
  const result = useQuery<string, AxiosError>({
    queryKey: ["logo", projectId, companyId],
    queryFn: async () => {
      const url = `/projects/${projectId}/companies/${companyId}/logo`;
      return (await axios.get<string>(url)).data;
    },
    refetchInterval: false,
    enabled: !!projectId,
  });
  return result;
};
