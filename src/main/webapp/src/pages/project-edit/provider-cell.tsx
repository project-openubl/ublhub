import React from "react";
import { useKeyQuery } from "queries/keys";

interface IProviderCellProps {
  projectId: string;
  companyId: string | null;
  keyId: string;
}

export const ProviderCell: React.FC<IProviderCellProps> = ({
  projectId,
  companyId,
  keyId,
}) => {
  const keysQuery = useKeyQuery(projectId, companyId, keyId);

  return <>{keysQuery.data?.name}</>;
};
