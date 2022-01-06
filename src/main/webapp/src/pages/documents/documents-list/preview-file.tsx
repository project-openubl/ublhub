import React from "react";
import { Modal, ModalVariant } from "@patternfly/react-core";
import { DownloadIcon } from "@patternfly/react-icons";
import { CodeEditorControl, Language } from "@patternfly/react-code-editor";

import { ResolvedQueries } from "@konveyor/lib-ui";

import { YAMLEditor } from "shared/components";
import { usePausedPollingEffect } from "shared/context";

import { useDocumentUBLFileQuery as useDocumentFileQuery } from "queries/documents";

import { UBLDocument } from "api/ublhub";
import { prettifyXML } from "utils/modelUtils";

interface IPreviewUBLFileProps {
  namespaceId: string;
  documentBeingEdited: UBLDocument;
  variant: "ubl" | "cdr";
  onClose: () => void;
}

export const PreviewFile: React.FC<IPreviewUBLFileProps> = ({
  namespaceId,
  documentBeingEdited,
  variant,
  onClose,
}) => {
  usePausedPollingEffect();

  const documentFileQuery = useDocumentFileQuery(
    namespaceId,
    documentBeingEdited.id || "",
    variant,
    "xml"
  );

  const downloadOriginalFile = () => {
    if (!documentFileQuery.data) {
      return;
    }

    const value = documentFileQuery.data;
    const element = document.createElement("a");
    const file = new Blob([value], { type: "text" });
    element.href = URL.createObjectURL(file);
    element.download = `${
      documentBeingEdited.fileContent?.documentID || "file"
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
    <Modal
      isOpen
      variant={ModalVariant.large}
      title={variant.toUpperCase()}
      onClose={() => onClose()}
    >
      <ResolvedQueries
        resultsWithErrorTitles={[
          { result: documentFileQuery, errorTitle: "Cannot load file" },
        ]}
      >
        <YAMLEditor
          minHeight={500}
          value={prettifyXML(documentFileQuery.data || "")}
          codeEditorProps={{
            isReadOnly: true,
            language: Language.xml,
            isUploadEnabled: false,
            isDownloadEnabled: false,
            customControls: downloadBtnControl,
          }}
        />
      </ResolvedQueries>
    </Modal>
  );
};
