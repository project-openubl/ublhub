import { Input } from "api/ublhub";

export interface InputTemplate {
  category: Category;
  metadata: Metadata;
  input: Input;
}

type Category =
  | "Invoice"
  | "CreditNote"
  | "DebitNote"
  | "VoidedDocument"
  | "SummaryDocument"
  | "Perception"
  | "Retention";

interface Metadata {
  title: string;
  description: string;
}

//

export const InputData: InputTemplate[] = [
  {
    category: "Invoice",
    metadata: {
      title: "Basic invoice",
      description: "Create a basic invoice with minimal data",
    },
    input: {
      kind: "Invoice",
      spec: {
        idGenerator: {
          name: "none",
        },
        document: {
          serie: "F001",
          numero: 1,
          proveedor: {
            ruc: "12345678912",
            razonSocial: "Mi razón social S.A.C.",
          },
          cliente: {
            nombre: "Nombre de mi cliente",
            numeroDocumentoIdentidad: "12121212121",
            tipoDocumentoIdentidad: "RUC",
          },
          detalle: [
            {
              descripcion: "Item1",
              cantidad: 10,
              precioUnitario: 100,
            },
            {
              descripcion: "Item2",
              cantidad: 10,
              precioUnitario: 100,
            },
          ],
        },
      },
    },
  },
  {
    category: "CreditNote",
    metadata: {
      title: "Basic credit note",
      description: "Create a basic credit note with minimal data",
    },
    input: {
      kind: "CreditNote",
      spec: {
        idGenerator: {
          name: "none",
        },
        document: {
          serie: "FC01",
          numero: 1,
          serieNumeroComprobanteAfectado: "F001-1",
          descripcionSustentoDeNota: "Sustento de nota",
          proveedor: {
            ruc: "12345678912",
            razonSocial: "Mi razón social S.A.C.",
          },
          cliente: {
            nombre: "Nombre de mi cliente",
            numeroDocumentoIdentidad: "12121212121",
            tipoDocumentoIdentidad: "RUC",
          },
          detalle: [
            {
              descripcion: "Item1",
              cantidad: 10,
              precioUnitario: 100,
            },
            {
              descripcion: "Item2",
              cantidad: 10,
              precioUnitario: 100,
            },
          ],
        },
      },
    },
  },
  {
    category: "DebitNote",
    metadata: {
      title: "Basic debit note",
      description: "Create a basic debit note with minimal data",
    },
    input: {
      kind: "DebitNote",
      spec: {
        idGenerator: {
          name: "none",
        },
        document: {
          serie: "FD01",
          numero: 1,
          serieNumeroComprobanteAfectado: "F001-1",
          descripcionSustentoDeNota: "Sustento de nota",
          proveedor: {
            ruc: "12345678912",
            razonSocial: "Mi razón social S.A.C.",
          },
          cliente: {
            nombre: "Nombre de mi cliente",
            numeroDocumentoIdentidad: "12121212121",
            tipoDocumentoIdentidad: "RUC",
          },
          detalle: [
            {
              descripcion: "Item1",
              cantidad: 10,
              precioUnitario: 100,
            },
            {
              descripcion: "Item2",
              cantidad: 10,
              precioUnitario: 100,
            },
          ],
        },
      },
    },
  },
  {
    category: "VoidedDocument",
    metadata: {
      title: "Basic voided document",
      description: "Create a basic voided document with minimal data",
    },
    input: {
      kind: "VoidedDocument",
      spec: {
        idGenerator: {
          name: "none",
        },
        document: {
          numero: 1,
          proveedor: {
            ruc: "12345678912",
            razonSocial: "Mi razón social S.A.C.",
          },
          descripcionSustento: "mi razon de baja",
          comprobante: {
            serieNumero: "F001-1",
            tipoComprobante: "factura",
            fechaEmision: 1641394054167,
          },
        },
      },
    },
  },
  {
    category: "SummaryDocument",
    metadata: {
      title: "Basic summary document",
      description: "Create a basic summary document with minimal data",
    },
    input: {
      kind: "SummaryDocument",
      spec: {
        idGenerator: {
          name: "none",
        },
        document: {
          numero: 1,
          fechaEmisionDeComprobantesAsociados: 1641394054167,
          proveedor: {
            ruc: "12345678912",
            razonSocial: "Mi razón social S.A.C.",
          },
          detalle: [
            {
              tipoOperacion: "ADICIONAR",
              comprobante: {
                tipo: "BOLETA",
                serieNumero: "B001-1",
                cliente: {
                  nombre: "nombre de mi cliente",
                  numeroDocumentoIdentidad: "12345678",
                  tipoDocumentoIdentidad: "DNI",
                },
                impuestos: {
                  igv: 100,
                },
                valorVenta: {
                  importeTotal: 118,
                  gravado: 100,
                },
              },
            },
            {
              tipoOperacion: "ADICIONAR",
              comprobante: {
                tipo: "NOTA_CREDITO",
                serieNumero: "BC01-1",
                cliente: {
                  nombre: "nombre de mi cliente",
                  numeroDocumentoIdentidad: "12345678",
                  tipoDocumentoIdentidad: "DNI",
                },
                impuestos: {
                  igv: 100,
                },
                valorVenta: {
                  importeTotal: 118,
                  gravado: 100,
                },
              },
              comprobanteAfectado: {
                tipo: "BOLETA",
                serieNumero: "B001-1",
              },
            },
          ],
        },
      },
    },
  },
];
