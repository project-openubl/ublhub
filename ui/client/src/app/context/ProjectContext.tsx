import React, { createContext, useContext, useState } from "react";

interface IProjectContext {
  projectId?: number | string;
  setProjectId: (projectId: number | string) => void;
}

const ProjectContext = createContext<IProjectContext>({
  projectId: undefined,
  setProjectId: () => undefined,
});

interface IProjectContextProviderProps {
  children: React.ReactNode;
}

export const ProjectContextProvider: React.FC<IProjectContextProviderProps> = ({
  children,
}: IProjectContextProviderProps) => {
  const [projectid, setProjectId] = useState<number | string>();

  return (
    <ProjectContext.Provider
      value={{
        projectId: projectid,
        setProjectId: (key: number | string) => setProjectId(key),
      }}
    >
      {children}
    </ProjectContext.Provider>
  );
};

export const useProjectContext = (): IProjectContext =>
  useContext(ProjectContext);
