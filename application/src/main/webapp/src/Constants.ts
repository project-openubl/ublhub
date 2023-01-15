export const KEY_PROVIDERS = "keyProviders";

export interface ISunatCredentials {
  username: string;
  password: string;
}

export interface SunatURls {
  factura: string;
  guia: string;
  retencion: string;
}

export const SUNAT_BETA_CREDENTIALS: ISunatCredentials = {
  username: "MODDATOS",
  password: "MODDATOS",
};

export const SUNAT_PROD_URLS: SunatURls = {
  factura: "https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl",
  guia: "https://api-cpe.sunat.gob.pe/v1/contribuyente/gem",
  retencion:
    "https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl",
};

export const SUNAT_BETA_URLS: SunatURls = {
  factura: "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService",
  guia: "https://api-cpe.sunat.gob.pe/v1/contribuyente/gem",
  retencion:
    "https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService",
};
