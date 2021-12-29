import {
  ClusterResource,
  IGroupVersionKindPlural,
  NamespacedResource,
} from "./common";

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

export class CoreNamespacedResource extends NamespacedResource {
  private _gvk: IGroupVersionKindPlural;
  constructor(kind: CoreNamespacedResourceKind, namespace: string) {
    super(namespace);

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
    // The core resources live at a unique api path for legacy reasons, and do
    // not have an API group
    return [
      this.gvk().version,
      "namespaces",
      this.namespace,
      this.gvk().kindPlural,
    ].join("/");
  }
}

export enum CoreClusterResourceKind {
  Namespace = "namespaces",
  ServerInfo = "server-info",
}

export enum CoreNamespacedResourceKind {
  Key = "keys",
  Component = "components",
}
