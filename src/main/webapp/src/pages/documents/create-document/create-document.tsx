import React, { useCallback, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import Measure from "react-measure";
import { dump, load } from "js-yaml";
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
import { CodeEditor, Language } from "@patternfly/react-code-editor";

import {
  BreadCrumbPath,
  NamespaceContextSelector,
  SimplePageSection,
  useNamespaceContext,
} from "shared/components";

import { documentsPath, INamespaceParams } from "Paths";
import { Templates } from "./templates";
import { PreviewXML } from "./preview-xml";
import { usePreviewDocumentMutation } from "queries/documents";
import { useParams } from "react-router-dom";
import { Input } from "api/ublhub";

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

  const routeParams = useParams<INamespaceParams>();
  const prefillNamespaceId = routeParams.namespaceId;

  const previewDocumentMutation =
    usePreviewDocumentMutation(prefillNamespaceId);

  //
  const namespaceContext = useNamespaceContext();

  // Editor
  const editorRef = useRef<monacoEditor.editor.IStandaloneCodeEditor | null>(
    null
  );
  const [initialEditorValue, setInitialEditorValue] =
    useState(defaultEditorValue);

  const onEditorDidMount = (
    editor: monacoEditor.editor.IStandaloneCodeEditor,
    monaco: typeof monacoEditor
  ) => {
    editorRef.current = editor;

    editor.layout();
    editor.focus();

    monaco.editor.getModels()[0].updateOptions({ tabSize: 2 });
  };

  const getEditorValue = useCallback(() => {
    return editorRef.current?.getValue();
  }, [editorRef]);

  // Tabs
  const [activeMainTab, setActiveMainTab] = useState(MainTab.EditFile);

  // Input

  const createPreview = () => {
    const object = load(getEditorValue() || "");
    previewDocumentMutation.mutate(object as Input);
  };

  return (
    <NamespaceContextSelector redirect={documentsPath}>
      <PageSection variant="light" type="breadcrumb">
        <BreadCrumbPath
          breadcrumbs={[
            {
              title: "Documents",
              path: namespaceContext ? documentsPath(namespaceContext) : "/",
            },
            { title: t("actions.create"), path: "/" },
          ]}
        />
      </PageSection>
      <SimplePageSection
        title="Crear documento"
        description="Create by manually entering YAML or JSON definitions, or  by dragging and dropping a file into the editor."
      />
      <PageSection>
        <Grid style={{ height: "100%" }}>
          <GridItem md={8}>
            <Card isFullHeight isCompact>
              <Measure bounds>
                {({ measureRef, contentRect }) => {
                  return (
                    <div className="pf-c-card__body" ref={measureRef}>
                      <Stack hasGutter>
                        <StackItem isFilled>
                          <Tabs
                            activeKey={activeMainTab}
                            onSelect={(_, tabIndex) => {
                              setActiveMainTab(tabIndex as MainTab);
                              createPreview();
                            }}
                          >
                            <Tab
                              eventKey={MainTab.EditFile}
                              title={
                                <>
                                  <TabTitleIcon>
                                    <CodeIcon />
                                  </TabTitleIcon>{" "}
                                  <TabTitleText>Edit file</TabTitleText>{" "}
                                </>
                              }
                            >
                              <br />
                              <CodeEditor
                                isDarkTheme
                                isMinimapVisible
                                isLineNumbersVisible
                                isLanguageLabelVisible
                                isUploadEnabled
                                isDownloadEnabled
                                isCopyEnabled
                                code={initialEditorValue}
                                language={Language.yaml}
                                onEditorDidMount={onEditorDidMount}
                                // Height is equal to Card heigth - (padding applied to content)
                                height={
                                  (contentRect.bounds?.height || 0) - 200 + "px"
                                }
                                customControls={[]}
                              />
                            </Tab>
                            <Tab
                              eventKey={MainTab.PreviewChanges}
                              title={
                                <>
                                  <TabTitleIcon>
                                    <EyeIcon />
                                  </TabTitleIcon>{" "}
                                  <TabTitleText>Preview changes</TabTitleText>{" "}
                                </>
                              }
                            >
                              <PreviewXML
                                value={previewDocumentMutation.data}
                              />
                            </Tab>
                          </Tabs>
                        </StackItem>
                        <StackItem>
                          <ActionGroup>
                            <Button
                              variant="primary"
                              onClick={() => {
                                console.log(
                                  (editorRef.current as any).getValue()
                                );
                              }}
                            >
                              {t("actions.save")}
                            </Button>
                            <Button variant="link">
                              {t("actions.cancel")}
                            </Button>
                          </ActionGroup>
                        </StackItem>
                      </Stack>
                    </div>
                  );
                }}
              </Measure>
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
      </PageSection>
    </NamespaceContextSelector>
  );
};
