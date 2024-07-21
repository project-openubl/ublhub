import { AxiosError } from "axios";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { New, Project } from "@app/api/models";
import {
  createProject,
  deleteProject,
  getProjectById,
  getProjects,
  updateProject as updateProject,
} from "@app/api/rest";

export const ProjectsQueryKey = "projects";

export const useFetchProjects = () => {
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: [ProjectsQueryKey],
    queryFn: getProjects,
  });
  return {
    projects: data || [],
    isFetching: isLoading,
    fetchError: error,
    refetch,
  };
};

export const useFetchProjectById = (id?: number | string) => {
  const { data, isLoading, error } = useQuery({
    queryKey: [ProjectsQueryKey, id],
    queryFn: () =>
      id === undefined ? Promise.resolve(undefined) : getProjectById(id),
    enabled: id !== undefined,
  });

  return {
    project: data,
    isFetching: isLoading,
    fetchError: error as AxiosError,
  };
};

export const useCreateProjectMutation = (
  onSuccess: (res: Project) => void,
  onError: (err: AxiosError, payload: New<Project>) => void
) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createProject,
    onSuccess: ({ data }, _payload) => {
      onSuccess(data);
      queryClient.invalidateQueries({ queryKey: [ProjectsQueryKey] });
    },
    onError,
  });
};

export const useUpdateProjectMutation = (
  onSuccess: (payload: Project) => void,
  onError: (err: AxiosError, payload: Project) => void
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateProject,
    onSuccess: (_res, payload) => {
      onSuccess(payload);
      queryClient.invalidateQueries({ queryKey: [ProjectsQueryKey] });
    },
    onError: onError,
  });
};

export const useDeleteProjectMutation = (
  onSuccess: (id: number | string) => void,
  onError: (err: AxiosError, id: number | string) => void
) => {
  const queryClient = useQueryClient();

  const { isPending, mutate, error } = useMutation({
    mutationFn: deleteProject,
    onSuccess: (_res, id) => {
      onSuccess(id);
      queryClient.invalidateQueries({ queryKey: [ProjectsQueryKey] });
    },
    onError: (err: AxiosError, id) => {
      onError(err, id);
      queryClient.invalidateQueries({ queryKey: [ProjectsQueryKey] });
    },
  });

  return {
    mutate,
    isPending,
    error,
  };
};
