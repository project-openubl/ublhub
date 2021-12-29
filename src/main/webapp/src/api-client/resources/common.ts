export type ApiResource = ClusterResource;

export interface IApiResource {
  listPath(): string;
  idPath(id: string): string;
}

export interface IGroupVersionKindPlural {
  group: string;
  version: string;
  kindPlural: string;
}

function idPath(listPath: string, name: string) {
  return [listPath, name].join("/");
}

export abstract class NamespacedResource implements IApiResource {
  public abstract gvk(): IGroupVersionKindPlural;
  public namespace: string;
  constructor(namespace: string) {
    if (!namespace) {
      throw new Error(
        "NamespacedResource must be passed a namespace, it was undefined"
      );
    }
    this.namespace = namespace;
  }

  public listPath(): string {
    return [
      "/api",
      this.gvk().group,
      this.gvk().version,
      "namespaces",
      this.namespace,
      this.gvk().kindPlural,
    ].join("/");
  }

  public idPath(name: string): string {
    return idPath(this.listPath(), name);
  }
}

export abstract class ClusterResource implements IApiResource {
  public abstract gvk(): IGroupVersionKindPlural;
  public listPath(): string {
    return [
      "/api",
      this.gvk().group,
      this.gvk().version,
      this.gvk().kindPlural,
    ].join("/");
  }
  public idPath(id: string): string {
    return idPath(this.listPath(), id);
  }
}
