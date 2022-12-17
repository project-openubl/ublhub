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
  guia: "https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService?wsdl",
  retencion:
    "https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl",
};

export const SUNAT_BETA_URLS: SunatURls = {
  factura: "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService",
  guia: "https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService",
  retencion:
    "https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService",
};
