import React from "react";

import {
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  Title,
} from "@patternfly/react-core";
import { DownloadIcon, SpinnerIcon } from "@patternfly/react-icons";
import {
  CodeEditor,
  CodeEditorControl,
  Language,
} from "@patternfly/react-code-editor";

import { useDocumentXmlCdrQuery } from "queries/documents";
import { DocumentDto } from "api/models";
import { prettifyXML } from "utils/modelUtils";

interface IXmlCdrPreviewProps {
  projectName: string;
  document: DocumentDto;
  variant: "xml" | "cdr";
}

export const XmlCdrPreview: React.FC<IXmlCdrPreviewProps> = ({
  projectName,
  document: documentDto,
  variant,
}) => {
  const documentXmlCdrMutation = useDocumentXmlCdrQuery(
    projectName,
    documentDto.id || null,
    variant
  );

  const downloadOriginalFile = () => {
    if (!documentXmlCdrMutation.data) {
      return;
    }

    const value = documentXmlCdrMutation.data;
    const element = document.createElement("a");
    const file = new Blob([value], { type: "text" });
    element.href = URL.createObjectURL(file);
    element.download = `${
      documentDto.status.xmlData?.serieNumero || "file"
    }-${variant}.xml`;
    document.body.appendChild(element); // Required for this to work in FireFox
    element.click();
  };

  const downloadBtnControl = (
    <CodeEditorControl
      icon={<DownloadIcon />}
      aria-label="Download"
      toolTipText="Download"
      onClick={downloadOriginalFile}
    />
  );

  return (
    <>
      <CodeEditor
        isDarkTheme
        isLineNumbersVisible
        isMinimapVisible
        isLanguageLabelVisible
        isUploadEnabled={false}
        isDownloadEnabled={false}
        isCopyEnabled
        language={Language.xml}
        code={prettifyXML(documentXmlCdrMutation.data || "")}
        height="500px"
        customControls={downloadBtnControl}
        emptyState={
          <EmptyState>
            <EmptyStateIcon icon={SpinnerIcon} />
            <Title headingLevel="h4" size="lg">
              Loading...
            </Title>
            <EmptyStateBody>
              This process might take some seconds.
            </EmptyStateBody>
          </EmptyState>
        }
      />
    </>
  );
};
