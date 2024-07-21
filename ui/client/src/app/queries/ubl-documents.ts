import { keepPreviousData, useQuery } from "@tanstack/react-query";

import { HubRequestParams } from "@app/api/models";
import { getUblDocuments } from "@app/api/rest";

export const UblDocumentsQueryKey = "documents";

export const useFetchUblDocuments = (
  projectId?: number | string,
  params: HubRequestParams = {}
) => {
  const { data, isLoading, error, refetch } = useQuery({
    enabled: projectId !== undefined,
    queryKey: [UblDocumentsQueryKey, projectId, params],
    queryFn: () => getUblDocuments(projectId, params),
    placeholderData: keepPreviousData,
  });
  return {
    result: {
      data: data?.data || [],
      total: data?.total ?? 0,
      params: data?.params ?? params,
    },
    isFetching: isLoading,
    fetchError: error,
    refetch,
  };
};
