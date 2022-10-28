import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { stringify, parse } from "yaml";

import {
  ActionGroup,
  Button,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateSecondaryActions,
  Form,
  Title,
} from "@patternfly/react-core";
import { CodeEditor, Language } from "@patternfly/react-code-editor";
import { CodeIcon } from "@patternfly/react-icons";

import { useCreateDocumentMutation } from "queries/documents";
import { DocumentDto } from "api/models";

import INVOICE from "jsons/invoice.json";
import CREDIT_NOTE from "jsons/creditNote.json";
import DEBIT_NOTE from "jsons/debitNote.json";
import { ResolvedQueries } from "@migtools/lib-ui";

interface IDocumentEditor {
  projectId: string;
  companyId?: string;
  onSaved: (instance: DocumentDto) => void;
  onCancel: () => void;
}

export const DocumentEditor: React.FC<IDocumentEditor> = ({
  projectId,
  companyId,
  onSaved,
  onCancel,
}) => {
  const { t } = useTranslation();
  const [code, setCode] = useState<string>();

  const createDocumentMutation = useCreateDocumentMutation(
    projectId,
    (instance) => {
      onSaved(instance);
    }
  );

  const onSaveForm = () => {
    const payload = parse(code || "");
    createDocumentMutation.mutate(payload);
  };

  return (
    <>
      <Form>
        <CodeEditor
          isDarkTheme
          isLineNumbersVisible
          isMinimapVisible
          isLanguageLabelVisible
          isUploadEnabled
          isDownloadEnabled
          isCopyEnabled
          language={Language.yaml}
          code={code}
          onChange={setCode}
          // onEditorDidMount={onEditorDidMount}
          height="500px"
          emptyState={
            <EmptyState>
              <EmptyStateIcon icon={CodeIcon} />
              <Title headingLevel="h4" size="lg">
                Start editing
              </Title>
              <EmptyStateBody>
                Drag and drop a file or upload one.
              </EmptyStateBody>
              <Button
                variant="primary"
                onClick={() => setCode("// Write your code here \n")}
              >
                Start from scratch
              </Button>
              <EmptyStateSecondaryActions>
                <Button
                  variant="link"
                  onClick={() => setCode(stringify(INVOICE))}
                >
                  Invoice
                </Button>
                <Button
                  variant="link"
                  onClick={() => setCode(stringify(CREDIT_NOTE))}
                >
                  Credit Note
                </Button>
                <Button
                  variant="link"
                  onClick={() => setCode(stringify(DEBIT_NOTE))}
                >
                  Debit Note
                </Button>
              </EmptyStateSecondaryActions>
            </EmptyState>
          }
        />

        <ResolvedQueries
          resultsWithErrorTitles={[
            {
              result: createDocumentMutation,
              errorTitle: "Could not save data",
            },
          ]}
          spinnerMode="inline"
        />

        <ActionGroup>
          <Button
            variant="primary"
            onClick={onSaveForm}
            isDisabled={!code || createDocumentMutation.isLoading}
          >
            {t("actions.save")}
          </Button>
          <Button variant="link" onClick={onCancel}>
            {t("actions.cancel")}
          </Button>
        </ActionGroup>
      </Form>
    </>
  );
};
