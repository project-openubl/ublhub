import { useCallback } from "react";
import axios, { AxiosError } from "axios";
import * as yup from "yup";
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from "react-query";

import { ProjectDto } from "api/models";
import { usePollingContext } from "shared/context";

export const useProjectQuery = (
  id: string | null
): UseQueryResult<ProjectDto, AxiosError> => {
  const result = useQuery<ProjectDto, AxiosError>({
    queryKey: ["project", id],
    queryFn: async () => {
      return (await axios.get<ProjectDto>(`/projects/${id}`)).data;
    },
    enabled: !!id,
    retry: false,
    refetchOnMount: true,
  });
  return result;
};

export const useProjectsQuery = (): UseQueryResult<
  ProjectDto[],
  AxiosError
> => {
  const sortListByNameCallback = useCallback(
    (data: ProjectDto[]): ProjectDto[] =>
      [...data].sort((a, b) => a.name.localeCompare(b.name)),
    []
  );

  const result = useQuery<ProjectDto[], AxiosError>({
    queryKey: ["projects"],
    queryFn: async () => {
      return (await axios.get<ProjectDto[]>("/projects")).data;
    },
    refetchInterval: usePollingContext().refetchInterval,
    select: sortListByNameCallback,
  });
  return result;
};

export const useCreateProjectMutation = (
  onSuccess?: (project: ProjectDto) => void
): UseMutationResult<ProjectDto, AxiosError, ProjectDto> => {
  const queryClient = useQueryClient();
  return useMutation<ProjectDto, AxiosError, ProjectDto>(
    async (project) => {
      return (await axios.post<ProjectDto>("/projects", project)).data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries(["projects"]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useUpdateProjectMutation = (
  onSuccess?: (project: ProjectDto) => void
): UseMutationResult<ProjectDto, AxiosError, ProjectDto> => {
  const queryClient = useQueryClient();
  return useMutation<ProjectDto, AxiosError, ProjectDto>(
    async (project) => {
      return (await axios.put<ProjectDto>(`/projects/${project.name}`, project))
        .data;
    },
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries(["projects"]);
        onSuccess && onSuccess(response);
      },
    }
  );
};

export const useDeleteProjectMutation = (
  onSuccess?: () => void
): UseMutationResult<void, AxiosError, ProjectDto, unknown> => {
  const queryClient = useQueryClient();

  return useMutation<void, AxiosError, ProjectDto>(
    async (project: ProjectDto) => {
      await axios.delete<void>(`/projects/${project.name}`);
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(["projects"]);
        onSuccess && onSuccess();
      },
    }
  );
};

export const getProjectNameSchema = (
  projectsQuery: UseQueryResult<ProjectDto[]>,
  projectBeingPrefilled: ProjectDto | null
): yup.StringSchema =>
  yup
    .string()
    .required()
    .trim()
    .min(3)
    .max(250)
    .test("unique-name", "A project with this name already exists", (value) => {
      if (projectBeingPrefilled?.name === value) {
        return true;
      }
      if (projectsQuery.data?.find((ns) => ns.name === value)) {
        return false;
      }

      return true;
    });
