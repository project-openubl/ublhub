import { ClusterResource, IGroupVersionKindPlural } from "./common";

export class CoreClusterResource extends ClusterResource {
  private _gvk: IGroupVersionKindPlural;

  constructor(kind: CoreClusterResourceKind) {
    super();
    this._gvk = {
      group: "",
      version: "",
      kindPlural: kind,
    };
  }

  gvk(): IGroupVersionKindPlural {
    return this._gvk;
  }

  public listPath(): string {
    return [this.gvk().kindPlural].join("/");
  }
}

export enum CoreClusterResourceKind {
  Namespace = "namespaces",
}
