import React, { useCallback } from "react";
import Measure from "react-measure";
import {
  CodeEditor,
  CodeEditorProps,
  Language,
} from "@patternfly/react-code-editor";
import * as monacoEditor from "monaco-editor/esm/vs/editor/editor.api";

const defaultEditorOptions = {
  // readOnly: false,
  scrollBeyondLastLine: false,
};

interface IYAMLEditorProps {
  value?: string;
  minHeight?: string | number;
  onEditorDidMountAndSetup?: (
    editor: monacoEditor.editor.IStandaloneCodeEditor,
    monaco: typeof monacoEditor
  ) => void;
  codeEditorProps?: Partial<
    Omit<CodeEditorProps, "ref" | "options" | "code" | "height" | "width">
  >;
}

export const YAMLEditor: React.FC<IYAMLEditorProps> = ({
  value,
  minHeight = 400,
  onEditorDidMountAndSetup,
  codeEditorProps,
}) => {
  const editorDidMount = useCallback(
    (
      editor: monacoEditor.editor.IStandaloneCodeEditor,
      monaco: typeof monacoEditor
    ) => {
      editor.layout();
      editor.focus();
      monaco.editor.getModels()[0].updateOptions({ tabSize: 2 });

      onEditorDidMountAndSetup && onEditorDidMountAndSetup(editor, monaco);
    },
    [onEditorDidMountAndSetup]
  );

  return (
    <Measure bounds>
      {({ measureRef, contentRect }) => (
        <div
          ref={measureRef}
          className="ocs-yaml-editor__root"
          style={{ minHeight }}
        >
          <div className="ocs-yaml-editor__wrapper">
            <CodeEditor
              // ref={null}
              isDarkTheme
              isCopyEnabled
              isMinimapVisible
              isLineNumbersVisible
              isLanguageLabelVisible
              isUploadEnabled
              isDownloadEnabled
              language={Language.yaml}
              code={value}
              options={defaultEditorOptions}
              onEditorDidMount={editorDidMount}
              height={(contentRect.bounds?.height || 0) - 60 + "px"}
              {...codeEditorProps}
            />
          </div>
        </div>
      )}
    </Measure>
  );
};
