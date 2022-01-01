export interface UBLDocument {
  id?: string;
  createdOn: number;
  inProgress: boolean;
  error?: string;
  scheduledDelivery?: number;
  retryCount: number;

  fileContentValid?: boolean;
  fileContentValidationError?: string;
  fileContent?: UBLDocumentFileContent;

  sunat?: UBLDocumentSunat;
  sunatEvents: UBLDocumentEvent[];
}

export interface UBLDocumentFileContent {
  ruc: string;
  documentID: string;
  documentType: string;
}

export interface UBLDocumentSunat {
  code: string;
  status: "ACEPTADO" | "RECHAZADO" | "EXCEPCION" | "BAJA" | "EN_PROCESO";
  description: string;
  ticket: string;
  hasCdr: boolean;
}

export interface UBLDocumentEvent {
  status: "default" | "success" | "danger" | "warning" | "info";
  description: string;
  createdOn: number;
}

//

export interface Input {
  kind: Category;
  spec: InputSpec;
}

export interface InputSpec {
  idGenerator?: IDGenerator;
  signature?: SignatureGenerator;
  document:
    | InvoiceInputModel
    | CreditNoteInputModel
    | DebitNoteInputModel
    | SummaryDocumentInputModel
    | VoidedDocumentInputModel;
}

export interface IDGenerator {
  name: "none" | "generated";
  config: { [key: string]: string[] };
}

export interface SignatureGenerator {
  algorithm: string;
}

/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.34.976 on 2021-12-31 08:54:26.

export interface InvoiceInputModel extends DocumentInputModel {
  cuotasDePago?: CuotaDePagoInputModel[];
  otrosDocumentosTributariosRelacionados?: DocTribRelacionadoInputModel_Invoice[];
  anticipos?: AnticipoInputModel[];
}

export interface CreditNoteInputModel extends NoteInputModel {
  tipoNota: string;
  cuotasDePago: CuotaDePagoInputModel[];
  otrosDocumentosTributariosRelacionados: DocTribRelacionadoInputModel_CreditNote[];
}

export interface DebitNoteInputModel extends NoteInputModel {
  tipoNota: string;
  otrosDocumentosTributariosRelacionados: DocTribRelacionadoInputModel_DebitNote[];
}

export interface SummaryDocumentInputModel {
  numero: number;
  fechaEmision: number;
  fechaEmisionDeComprobantesAsociados: number;
  firmante: FirmanteInputModel;
  proveedor: ProveedorInputModel;
  detalle: SummaryDocumentLineInputModel[];
}

export interface VoidedDocumentInputModel {
  numero: number;
  fechaEmision: number;
  firmante: FirmanteInputModel;
  proveedor: ProveedorInputModel;
  descripcionSustento: string;
  comprobante: VoidedDocumentLineInputModel;
}

export interface ClienteInputModel {
  tipoDocumentoIdentidad: string;
  numeroDocumentoIdentidad: string;
  nombre: string;
  direccion?: DireccionInputModel;
  contacto?: ContactoInputModel;
}

export interface ProveedorInputModel {
  ruc: string;
  nombreComercial?: string;
  razonSocial: string;
  direccion?: DireccionInputModel;
  contacto?: ContactoInputModel;
}

export interface FirmanteInputModel {
  ruc: string;
  razonSocial: string;
}

export interface DocumentLineInputModel {
  descripcion: string;
  unidadMedida?: string;
  cantidad: number;
  precioUnitario?: number;
  precioConIgv?: number;
  tipoIgv?: string;
  icb?: boolean;
}

export interface GuiaRemisionRelacionadaInputModel {
  serieNumero: string;
  tipoDocumento: string;
}

export interface CuotaDePagoInputModel {
  monto: number;
  porcentaje: number;
  fechaPago: number;
}

export interface DocTribRelacionadoInputModel_Invoice
  extends DocTribRelacionadoInputModel {}

export interface AnticipoInputModel extends DocTribRelacionadoInputModel {
  montoTotal: number;
}

export interface DocumentInputModel {
  serie: string;
  numero: number;
  fechaEmision?: number;
  cliente: ClienteInputModel;
  proveedor: ProveedorInputModel;
  firmante?: FirmanteInputModel;
  detalle: DocumentLineInputModel[];
  guiasRemisionRelacionadas?: GuiaRemisionRelacionadaInputModel[];
  otrosDocumentosTributariosRelacionados?: DocTribRelacionadoInputModel[];
}

export interface DocTribRelacionadoInputModel_CreditNote
  extends DocTribRelacionadoInputModel {}

export interface NoteInputModel extends DocumentInputModel {
  serieNumeroComprobanteAfectado: string;
  descripcionSustentoDeNota: string;
}

export interface DocTribRelacionadoInputModel_DebitNote
  extends DocTribRelacionadoInputModel {}

export interface SummaryDocumentLineInputModel {
  tipoOperacion: string;
  comprobante: SummaryDocumentComprobanteInputModel;
  comprobanteAfectado: SummaryDocumentComprobanteAfectadoInputModel;
}

export interface VoidedDocumentLineInputModel {
  serieNumero: string;
  tipoComprobante: string;
  fechaEmision: number;
}

export interface DireccionInputModel {
  ubigeo: string;
  codigoLocal: string;
  urbanizacion: string;
  provincia: string;
  departamento: string;
  distrito: string;
  direccion: string;
  codigoPais: string;
}

export interface ContactoInputModel {
  telefono: string;
  email: string;
}

export interface DocTribRelacionadoInputModel {
  serieNumero: string;
  tipoDocumento: string;
}

export interface SummaryDocumentComprobanteInputModel {
  tipo: string;
  serieNumero: string;
  cliente: ClienteInputModel;
  valorVenta: SummaryDocumentComprobanteValorVentaInputModel;
  impuestos: SummaryDocumentImpuestosInputModel;
}

export interface SummaryDocumentComprobanteAfectadoInputModel {
  tipo: string;
  serieNumero: string;
}

export interface SummaryDocumentComprobanteValorVentaInputModel {
  importeTotal: number;
  otrosCargos: number;
  gravado: number;
  exonerado: number;
  inafecto: number;
  gratuito: number;
}

export interface SummaryDocumentImpuestosInputModel {
  igv: number;
  icb: number;
}
