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
