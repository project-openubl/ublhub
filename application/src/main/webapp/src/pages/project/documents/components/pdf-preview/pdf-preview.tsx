import React from "react";

import { useDocumentPdfUrlQuery } from "queries/documents";
import { DocumentDto } from "api/models";

import { defaultLayoutPlugin } from "@react-pdf-viewer/default-layout";
import { Worker } from "@react-pdf-viewer/core";
import { Viewer } from "@react-pdf-viewer/core";
import "@react-pdf-viewer/core/lib/styles/index.css";
import "@react-pdf-viewer/default-layout/lib/styles/index.css";

interface IPdfPreviewProps {
  projectName: string;
  document: DocumentDto;
}

export const PdfPreview: React.FC<IPdfPreviewProps> = ({
  projectName,
  document: documentDto,
}) => {
  const defaultLayoutPluginInstance = defaultLayoutPlugin();
  const pdfUrlQuery = useDocumentPdfUrlQuery(projectName, documentDto.id || null);

  return (
    <>
      {pdfUrlQuery.data && (
        <Worker workerUrl="https://unpkg.com/pdfjs-dist@3.5.141/build/pdf.worker.min.js">
          <Viewer
            fileUrl={pdfUrlQuery.data}
            plugins={[defaultLayoutPluginInstance]}
          />
        </Worker>
      )}
    </>
  );
};
