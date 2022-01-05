import React, { useCallback, useRef, useState } from "react";
import { useHistory, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { dump, load } from "js-yaml";
import * as prettier from "prettier/standalone";
import * as prettierXmlPlugin from "@prettier/plugin-xml";
import * as monacoEditor from "monaco-editor/esm/vs/editor/editor.api";

import {
  ActionGroup,
  Button,
  Card,
  CardBody,
  Grid,
  GridItem,
  PageSection,
  Stack,
  StackItem,
  Tab,
  TabContentBody,
  Tabs,
  TabTitleIcon,
  TabTitleText,
} from "@patternfly/react-core";
import { CodeIcon, EyeIcon } from "@patternfly/react-icons";
import { Language } from "@patternfly/react-code-editor";

import { ResolvedQuery } from "@konveyor/lib-ui";

import {
  BreadCrumbPath,
  NamespaceContextSelector,
  SimplePageSection,
  YAMLEditor,
} from "shared/components";

import {
  useCreateDocumentMutation,
  usePreviewDocumentMutation,
} from "queries/documents";

import { Input } from "api/ublhub";
import { documentsPath, INamespaceParams } from "Paths";
import { Templates } from "./templates";

enum MainTab {
  EditFile,
  PreviewChanges,
}

const defaultEditorValue: string = dump({
  kind: "Invoice",
  spec: {
    document: {},
  },
});

export const CreateDocument: React.FC = () => {
  const { t } = useTranslation();

  const history = useHistory();
  const routeParams = useParams<INamespaceParams>();
  const namespaceId = routeParams.namespaceId;

  const previewDocumentMutation = usePreviewDocumentMutation(namespaceId);
  const createDocumentMutation = useCreateDocumentMutation(namespaceId, () => {
    history.push(documentsPath(namespaceId));
  });

  // Editor
  const editorRef = useRef<monacoEditor.editor.IStandaloneCodeEditor | null>(
    null
  );
  const [initialEditorValue, setInitialEditorValue] =
    useState(defaultEditorValue);

  const onEditorDidMountAndSetup = (
    editor: monacoEditor.editor.IStandaloneCodeEditor
  ) => {
    editorRef.current = editor;
  };

  const getEditorValue = useCallback(() => {
    return editorRef.current?.getValue();
  }, [editorRef]);

  // Tabs
  const [activeMainTab, setActiveMainTab] = useState(MainTab.EditFile);
  const onTabChange = (
    event: React.MouseEvent<HTMLElement, MouseEvent>,
    eventKey: number | string
  ) => {
    const newTab = eventKey as MainTab;
    setActiveMainTab(newTab);
    if (newTab !== MainTab.EditFile) {
      onCreatePreview();
    }
  };

  // Input
  const getInput = () => {
    const object = load(getEditorValue() || "");
    return object as Input;
  };

  const onCreatePreview = () => {
    previewDocumentMutation.mutate(getInput());
  };

  const onSave = () => {
    createDocumentMutation.mutate(getInput());
  };

  const onCancel = () => {
    history.push(documentsPath(namespaceId));
  };

  return (
    <NamespaceContextSelector redirect={(ns) => documentsPath(ns.id)}>
      <PageSection variant="light" type="breadcrumb">
        <BreadCrumbPath
          breadcrumbs={[
            {
              title: "Documents",
              path: documentsPath(namespaceId),
            },
            { title: t("actions.create"), path: "/" },
          ]}
        />
      </PageSection>
      <SimplePageSection title="Crear documento" />
      <PageSection>
        <Stack hasGutter>
          <StackItem isFilled>
            <Grid style={{ height: "100%" }}>
              <GridItem md={8}>
                <Card isFullHeight isCompact>
                  <CardBody>
                    <Tabs activeKey={activeMainTab} onSelect={onTabChange}>
                      <Tab
                        eventKey={MainTab.EditFile}
                        title={
                          <>
                            <TabTitleIcon>
                              <CodeIcon />
                            </TabTitleIcon>{" "}
                            <TabTitleText>Editar archivo</TabTitleText>{" "}
                          </>
                        }
                        className="yaml-wrapper"
                      >
                        <TabContentBody className="yaml-editor">
                          <YAMLEditor
                            value={initialEditorValue}
                            onEditorDidMountAndSetup={onEditorDidMountAndSetup}
                          />
                        </TabContentBody>
                      </Tab>
                      <Tab
                        eventKey={MainTab.PreviewChanges}
                        title={
                          <>
                            <TabTitleIcon>
                              <EyeIcon />
                            </TabTitleIcon>{" "}
                            <TabTitleText>Previsualizar cambios</TabTitleText>{" "}
                          </>
                        }
                        className="yaml-wrapper"
                      >
                        <TabContentBody className="yaml-editor">
                          <ResolvedQuery
                            result={previewDocumentMutation}
                            errorTitle="Could not create preview"
                          >
                            <YAMLEditor
                              value={prettier.format(
                                previewDocumentMutation.data || "",
                                {
                                  parser: "xml",
                                  plugins: [prettierXmlPlugin],
                                  xmlWhitespaceSensitivity: "ignore",
                                } as any
                              )}
                              codeEditorProps={{
                                isReadOnly: true,
                                language: Language.xml,
                                isUploadEnabled: false,
                              }}
                            />
                          </ResolvedQuery>
                        </TabContentBody>
                      </Tab>
                    </Tabs>
                  </CardBody>
                </Card>
              </GridItem>

              <GridItem md={4}>
                <Card isFullHeight isCompact>
                  <CardBody>
                    <Tabs defaultActiveKey={0}>
                      <Tab eventKey={0} title="Marketplace">
                        <TabContentBody>
                          <Templates
                            onSelect={(input) => {
                              const yaml = dump(input);
                              setInitialEditorValue(yaml);
                            }}
                          />
                        </TabContentBody>
                      </Tab>
                    </Tabs>
                  </CardBody>
                </Card>
              </GridItem>
            </Grid>
          </StackItem>
          <StackItem>
            <ResolvedQuery
              result={createDocumentMutation}
              errorTitle="Could not create document"
              spinnerMode="inline"
            />
            <ActionGroup>
              <Button variant="primary" onClick={onSave}>
                {t("actions.save")}
              </Button>
              <Button variant="link" onClick={onCancel}>
                {t("actions.cancel")}
              </Button>
            </ActionGroup>
          </StackItem>
        </Stack>
      </PageSection>
    </NamespaceContextSelector>
  );
};
