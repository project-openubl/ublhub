/* eslint-disable @typescript-eslint/ban-types */
import { ApiResource } from "./resources/common";

import type { AxiosStatic, AxiosInstance, ResponseType } from "axios";

let axios: AxiosStatic;
try {
  axios = require("axios");
  // eslint-disable-next-line no-empty
} catch (e) {}

export interface IClusterClient {
  list(resource: ApiResource, params?: object): Promise<any>;
  get(resource: ApiResource, id: string, params?: object): Promise<any>;
  put(
    resource: ApiResource,
    id: string,
    updatedObject: object,
    params?: object
  ): Promise<any>;
  patch(
    resource: ApiResource,
    id: string,
    patch: object,
    params?: object
  ): Promise<any>;
  create(
    resource: ApiResource,
    newObject: object,
    params?: object
  ): Promise<any>;
  delete(resource: ApiResource, id: string, params?: object): Promise<any>;
  apiRoot: string;
}

export class ClusterClient {
  public apiRoot: string;
  private requester: AxiosInstance;

  constructor(apiRoot: string, customResponseType: ResponseType = "json") {
    this.apiRoot = apiRoot;
    this.requester = axios.create({
      baseURL: this.apiRoot,
      headers: {
        "Content-Type": "application/json",
      },
      transformResponse: undefined,
      responseType: customResponseType,
    });
  }

  public list = async (
    resource: ApiResource,
    params?: object
  ): Promise<any> => {
    try {
      return await this.requester.get(resource.listPath(), { params });
    } catch (err) {
      throw err;
    }
  };

  public get = async (
    resource: ApiResource,
    id: string,
    params?: object
  ): Promise<any> => {
    try {
      return await this.requester.get(resource.idPath(id), { params });
    } catch (err) {
      throw err;
    }
  };

  public put = async (
    resource: ApiResource,
    id: string,
    updatedObject: object,
    params?: object
  ): Promise<any> => {
    try {
      return await this.requester.put(resource.idPath(id), updatedObject, {
        params,
      });
    } catch (err) {
      throw err;
    }
  };

  public patch = async (
    resource: ApiResource,
    id: string,
    patch: object,
    params?: object
  ): Promise<any> => {
    try {
      return await this.requester.patch(resource.idPath(id), patch, { params });
    } catch (err) {
      throw err;
    }
  };

  public create = async (
    resource: ApiResource,
    newObject: object,
    params?: object
  ): Promise<any> => {
    try {
      return await this.requester.post(resource.listPath(), newObject, {
        params,
      });
    } catch (err) {
      throw err;
    }
  };

  public delete = async (
    resource: ApiResource,
    id: string,
    params?: object
  ): Promise<any> => {
    try {
      return await this.requester.delete(resource.idPath(id), { params });
    } catch (err) {
      throw err;
    }
  };
}
