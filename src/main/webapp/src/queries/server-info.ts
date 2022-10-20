import axios, { AxiosError } from "axios";
import { UseQueryResult, useQuery } from "react-query";

import { ServerInfoDto } from "api/models";

export const useServerInfoQuery = (): UseQueryResult<ServerInfoDto, AxiosError> => {
  const result = useQuery<ServerInfoDto, AxiosError>({
    queryKey: ["serverInfo"],
    queryFn: async () => {
      return (await axios.get<ServerInfoDto>("/server-info")).data;
    },
    refetchInterval: false,
  });
  return result;
};
