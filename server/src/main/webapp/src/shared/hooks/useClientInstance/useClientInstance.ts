import { ClusterClient, ClientFactory } from "api-client";

export const useClientInstance = (): ClusterClient => {
  const result = ClientFactory.cluster("/api");
  return result;
};

export default useClientInstance;
