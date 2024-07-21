import React from "react";
import { useTranslation } from "react-i18next";
import { NoDataEmptyState } from "./NoDataEmptyState";

export const StateNoData: React.FC = () => {
  const { t } = useTranslation();

  return (
    <NoDataEmptyState
      title={t("message.noDataAvailableTitle")}
      description={t("message.noDataAvailableBody")}
    />
  );
};
