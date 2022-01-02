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
];
