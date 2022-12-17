import React, { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { ResolvedQueries } from "@migtools/lib-ui";
import { useDebounce } from "usehooks-ts";
import { Pair, stringify, parse } from "yaml";
import * as monacoEditor from "monaco-editor/esm/vs/editor/editor.api";

import {
  ActionGroup,
  Button,
  Checkbox,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateSecondaryActions,
  Form,
  Grid,
  GridItem,
  Title,
  Toolbar,
  ToolbarContent,
  ToolbarItem,
} from "@patternfly/react-core";
import { CodeEditor, Language } from "@patternfly/react-code-editor";
import { CodeIcon } from "@patternfly/react-icons";

import {
  useCreateDocumentMutation,
  useEnrichDocumentMutation,
  useRenderDocumentMutation,
} from "queries/documents";
import { DocumentDto, DocumentInputType } from "api/models";
import { buildSchema } from "utils/schemautils";

import DocumentInputSchema from "schemas/DocumentInputDto-schema.json";
import InvoiceSchema from "schemas/Invoice-schema.json";
import CreditNoteSchema from "schemas/CreditNote-schema.json";
import DebitNoteSchema from "schemas/DebitNote-schema.json";
import VoidedDocumentsSchema from "schemas/VoidedDocuments-schema.json";
import SummaryDocumentsSchema from "schemas/SummaryDocuments-schema.json";
import PerceptionSchema from "schemas/Perception-schema.json";
import RetentionSchema from "schemas/Retention-schema.json";

import INVOICE from "jsons/invoice.json";
import CREDIT_NOTE from "jsons/creditNote.json";
import DEBIT_NOTE from "jsons/debitNote.json";
import VOIDED_DOCUMENTS from "jsons/voidedDocuments.json";
import SUMMARY_DOCUMENTS from "jsons/summaryDocuments.json";
import PERCEPTION from "jsons/perception.json";
import RETENTION from "jsons/retention.json";

const EDITOR_HEIGHT = "500px";

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

  const debouncedCode = useDebounce<string | undefined>(code, 500);
  const [enrichedCode, setEnrichedCode] = useState<string>();
  const [xmlCode, setXmlCode] = useState<string>();

  const [viewEnrichedCode, setViewEnrichedCode] = useState(false);
  const [viewXmlCode, setViewXmlCode] = useState(false);

  const { mutate: enrichDocumentMutate } = useEnrichDocumentMutation(
    projectId,
    (instance) => {
      const yamlString = stringify(instance, {
        sortMapEntries: (a: Pair<any, any>, b: Pair<any, any>) => {
          if (a.value.value !== undefined && b.value.value !== undefined) {
            const keyA: string = a.key?.value ?? "";
            const keyB: string = b.key?.value ?? "";
            return keyA.localeCompare(keyB);
          } else if (a.value.value !== undefined) {
            return -1;
          } else if (b.value.value !== undefined) {
            return 1;
          } else {
            return 0;
          }
        },
      });
      setEnrichedCode(yamlString);
    }
  );

  const { mutate: renderDocumentMutate } = useRenderDocumentMutation(
    projectId,
    (instance) => {
      setXmlCode(instance);
    }
  );

  const createDocumentMutation = useCreateDocumentMutation(
    projectId,
    (instance) => {
      onSaved(instance);
    }
  );

  useEffect(() => {
    if (viewEnrichedCode && debouncedCode) {
      try {
        const payload = JSON.parse(debouncedCode);
        enrichDocumentMutate(payload);
      } catch (error) {
        // nothing to do
      }
    }
  }, [viewEnrichedCode, debouncedCode, enrichDocumentMutate]);

  useEffect(() => {
    if (viewXmlCode && enrichedCode) {
      try {
        const payload = parse(enrichedCode);
        renderDocumentMutate(payload);
      } catch (error) {
        // nothing to do
      }
    }
  }, [viewXmlCode, enrichedCode, renderDocumentMutate]);

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
        case "VoidedDocuments":
          monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
            ...monaco.languages.json.jsonDefaults.diagnosticsOptions,
            schemas: [
              {
                uri: "http://myserver/foo-schema.json",
                fileMatch: ["*"],
                schema: buildSchema(VoidedDocumentsSchema),
              },
            ],
          });
          break;
        case "SummaryDocuments":
          monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
            ...monaco.languages.json.jsonDefaults.diagnosticsOptions,
            schemas: [
              {
                uri: "http://myserver/foo-schema.json",
                fileMatch: ["*"],
                schema: buildSchema(SummaryDocumentsSchema),
              },
            ],
          });
          break;
        case "Perception":
          monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
            ...monaco.languages.json.jsonDefaults.diagnosticsOptions,
            schemas: [
              {
                uri: "http://myserver/foo-schema.json",
                fileMatch: ["*"],
                schema: buildSchema(PerceptionSchema),
              },
            ],
          });
          break;
        case "Retention":
          monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
            ...monaco.languages.json.jsonDefaults.diagnosticsOptions,
            schemas: [
              {
                uri: "http://myserver/foo-schema.json",
                fileMatch: ["*"],
                schema: buildSchema(RetentionSchema),
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
      <Toolbar>
        <ToolbarContent>
          <ToolbarItem>
            <Checkbox
              id="view-enriched-code"
              label="Ver Enriched"
              isChecked={viewEnrichedCode}
              onChange={setViewEnrichedCode}
            />
          </ToolbarItem>
          <ToolbarItem>
            <Checkbox
              id="view-xml-code"
              label="Ver XML"
              isChecked={viewXmlCode}
              onChange={setViewXmlCode}
            />
          </ToolbarItem>
        </ToolbarContent>
      </Toolbar>
      <Form>
        <Grid
          hasGutter
          md={
            viewEnrichedCode && viewXmlCode
              ? 4
              : viewEnrichedCode || viewXmlCode
              ? 6
              : 12
          }
        >
          <GridItem>
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
              height={EDITOR_HEIGHT}
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
                    onClick={() => onSetDefaultCode({ kind: "" })}
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
                    <Button
                      variant="link"
                      onClick={() => onSetDefaultCode(VOIDED_DOCUMENTS)}
                    >
                      Voided Documents
                    </Button>
                    <Button
                      variant="link"
                      onClick={() => onSetDefaultCode(SUMMARY_DOCUMENTS)}
                    >
                      Summary Documents
                    </Button>
                    <Button
                      variant="link"
                      onClick={() => onSetDefaultCode(PERCEPTION)}
                    >
                      Perception
                    </Button>
                    <Button
                      variant="link"
                      onClick={() => onSetDefaultCode(RETENTION)}
                    >
                      Retention
                    </Button>
                  </EmptyStateSecondaryActions>
                </EmptyState>
              }
            />
          </GridItem>
          {viewEnrichedCode && (
            <GridItem>
              <CodeEditor
                isReadOnly
                isDarkTheme
                isLineNumbersVisible
                isMinimapVisible
                isLanguageLabelVisible
                isDownloadEnabled
                isCopyEnabled
                language={Language.yaml}
                code={enrichedCode}
                onEditorDidMount={() => {}}
                height={EDITOR_HEIGHT}
                emptyState={
                  <EmptyState isFullHeight>
                    <EmptyStateIcon icon={CodeIcon} />
                    <Title headingLevel="h4" size="lg">
                      Preview
                    </Title>
                    <EmptyStateBody>
                      See the values your XML will contain here.
                    </EmptyStateBody>
                  </EmptyState>
                }
              />
            </GridItem>
          )}
          {viewXmlCode && (
            <GridItem>
              <CodeEditor
                isReadOnly
                isDarkTheme
                isLineNumbersVisible
                isMinimapVisible
                isLanguageLabelVisible
                isDownloadEnabled
                isCopyEnabled
                language={Language.xml}
                code={xmlCode}
                onEditorDidMount={() => {}}
                height={EDITOR_HEIGHT}
                emptyState={
                  <EmptyState isFullHeight>
                    <EmptyStateIcon icon={CodeIcon} />
                    <Title headingLevel="h4" size="lg">
                      Preview
                    </Title>
                    <EmptyStateBody>
                      See the values your XML will contain here.
                    </EmptyStateBody>
                  </EmptyState>
                }
              />
            </GridItem>
          )}
        </Grid>

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
