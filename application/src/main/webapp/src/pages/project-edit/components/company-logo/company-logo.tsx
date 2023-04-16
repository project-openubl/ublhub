import React from "react";

import { Spinner } from "@patternfly/react-core";

import { useCompanyLogoQuery } from "queries/companies";

interface ICompanyLogoProps {
  projectId: string;
  companyId: string;
}

export const CompanyLogo: React.FC<ICompanyLogoProps> = ({
  projectId,
  companyId,
}) => {
  const logoQuery = useCompanyLogoQuery(projectId, companyId);

  let result;
  if (logoQuery.isLoading) {
    result = <Spinner size="sm" />;
  } else {
    if (logoQuery.data) {
      result = (
        <div style={{ maxHeight: 60, maxWidth: 60 }}>
          <img alt="Logo" src={`data:image/png;base64, ${logoQuery.data}`} />
        </div>
      );
    } else {
      result = <></>;
    }
  }

  return result;
};
