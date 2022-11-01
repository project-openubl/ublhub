import React, { useRef, useState } from "react";
import { useTranslation } from "react-i18next";

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

import { ResolvedQueries } from "@migtools/lib-ui";

import { useCreateDocumentMutation } from "queries/documents";
import { DocumentDto, DocumentInputType } from "api/models";

import DocumentInputSchema from "schemas/DocumentInputDto-schema.json";
import InvoiceSchema from "schemas/Invoice-schema.json";
import CreditNoteSchema from "schemas/CreditNote-schema.json";
import DebitNoteSchema from "schemas/DebitNote-schema.json";

import INVOICE from "jsons/invoice.json";
import CREDIT_NOTE from "jsons/creditNote.json";
import DEBIT_NOTE from "jsons/debitNote.json";

import * as monacoEditor from "monaco-editor/esm/vs/editor/editor.api";
import { buildSchema } from "utils/schemautils";

interface IDocumentEditor {
  projectId: string;
  onSaved: (instance: DocumentDto) => void;
  onCancel: () => void;
}

export const DocumentEditor: React.FC<IDocumentEditor> = ({
  projectId,
  onSaved,
  onCancel,
}) => {
  const { t } = useTranslation();
  const [code, setCode] = useState<string>();
  const monacoRef = useRef<typeof monacoEditor>();

  const createDocumentMutation = useCreateDocumentMutation(
    projectId,
    (instance) => {
      onSaved(instance);
    }
  );

  const onSetDefaultCode = (doc: any) => {
    setCode(JSON.stringify(doc, null, 2));
  };

  const onSaveForm = () => {
    const payload = JSON.parse(code || "");
    createDocumentMutation.mutate(payload);
  };

  const configureSchema = (value: string, monaco: typeof monacoEditor) => {
    try {
      const json = JSON.parse(value);
      const kind: DocumentInputType = json.kind;
      switch (kind) {
        case "Invoice":
          monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
            ...monaco.languages.json.jsonDefaults.diagnosticsOptions,
            schemas: [
              {
                uri: "http://myserver/foo-schema.json",
                fileMatch: ["*"],
                schema: buildSchema(InvoiceSchema),
              },
            ],
          });
          break;
        case "CreditNote":
          monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
            ...monaco.languages.json.jsonDefaults.diagnosticsOptions,
            schemas: [
              {
                uri: "http://myserver/foo-schema.json",
                fileMatch: ["*"],
                schema: buildSchema(CreditNoteSchema),
              },
            ],
          });
          break;
        case "DebitNote":
          monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
            ...monaco.languages.json.jsonDefaults.diagnosticsOptions,
            schemas: [
              {
                uri: "http://myserver/foo-schema.json",
                fileMatch: ["*"],
                schema: buildSchema(DebitNoteSchema),
              },
            ],
          });
          break;
        default:
          monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
            ...monaco.languages.json.jsonDefaults.diagnosticsOptions,
            schemas: [
              {
                uri: "http://myserver/foo-schema.json",
                fileMatch: ["*"],
                schema: DocumentInputSchema,
              },
            ],
          });
          break;
      }
    } catch (error) {
      // Nothing to do
    }
  };

  const editorDidMount = (
    editor: monacoEditor.editor.IStandaloneCodeEditor,
    monaco: typeof monacoEditor
  ) => {
    editor.layout();
    editor.focus();
    monaco.editor.getModels()[0].updateOptions({ tabSize: 2 });

    monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
      validate: true,
      schemaValidation: "warning",
      schemas: [
        {
          uri: "http://myserver/foo-schema.json",
          fileMatch: ["*"],
          schema: DocumentInputSchema,
        },
      ],
    });

    monacoRef.current = monaco;
    configureSchema(editor.getValue(), monaco);
  };

  const onCodeChange = (
    value: string,
    event: monacoEditor.editor.IModelContentChangedEvent
  ) => {
    setCode(value);

    if (monacoRef.current) {
      configureSchema(value, monacoRef.current);
    }
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
          language={Language.json}
          code={code}
          onChange={onCodeChange}
          onEditorDidMount={editorDidMount}
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
                  onClick={() => onSetDefaultCode(INVOICE)}
                >
                  Invoice
                </Button>
                <Button
                  variant="link"
                  onClick={() => onSetDefaultCode(CREDIT_NOTE)}
                >
                  Credit Note
                </Button>
                <Button
                  variant="link"
                  onClick={() => onSetDefaultCode(DEBIT_NOTE)}
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
