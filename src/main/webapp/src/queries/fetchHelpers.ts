import { useClientInstance } from "shared/hooks";
import { ClusterResource } from "api-client";
import { AxiosResponse } from "axios";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export const useSearchpeClient = () => {
  const client = useClientInstance();
  /* eslint-disable @typescript-eslint/ban-types */
  return {
    get: <T>(
      resource: ClusterResource,
      name: string,
      params?: object
    ): Promise<AxiosResponse<T>> => client.get(resource, name, params),
    list: <T>(
      resource: ClusterResource,
      params?: object
    ): Promise<AxiosResponse<T>> => client.list(resource, params),
    create: <T>(
      resource: ClusterResource,
      newObject: object,
      params?: object
    ): Promise<AxiosResponse<T>> => client.create(resource, newObject, params),
    delete: <T>(
      resource: ClusterResource,
      name: string,
      params?: object
    ): Promise<AxiosResponse<T>> => client.delete(resource, name, params),
    patch: <T>(
      resource: ClusterResource,
      name: string,
      patch: object,
      params?: object
    ): Promise<AxiosResponse<T>> => client.patch(resource, name, patch, params),
    put: <T>(
      resource: ClusterResource,
      name: string,
      object: object,
      params?: object
    ): Promise<AxiosResponse<T>> => client.put(resource, name, object, params),
  };
  /* eslint-enable @typescript-eslint/ban-types */
};
