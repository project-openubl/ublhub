import React, { useMemo } from "react";
import { useTranslation } from "react-i18next";

import {
  Button,
  ButtonVariant,
  Card,
  CardActions,
  CardBody,
  CardHeader,
  CardTitle,
  DescriptionList,
  DescriptionListDescription,
  DescriptionListGroup,
  DescriptionListTerm,
  Modal,
} from "@patternfly/react-core";

import { useModal } from "@project-openubl/lib-ui";

import { useCompaniesQuery } from "queries/companies";

import { CompanyDto, ProjectDto } from "api/models";
import { GeneralForm } from "./general-form";

interface IGeneralProps {
  project: ProjectDto;
  company: CompanyDto;
}

export const General: React.FC<IGeneralProps> = ({
  project,
  company: companyInput,
}) => {
  const { t } = useTranslation();
  const modal = useModal<"EDIT", CompanyDto>();

  const companiesQuery = useCompaniesQuery(project.id || null);
  const company = useMemo(() => {
    return companiesQuery.data?.find((e) => e.id === companyInput.id);
  }, [companyInput, companiesQuery.data]);

  return (
    <>
      <Card>
        <CardHeader>
          <CardActions>
            <Button
              variant={ButtonVariant.secondary}
              onClick={() => modal.open("EDIT")}
            >
              {t("actions.edit")}
            </Button>
          </CardActions>
          <CardTitle>Preview</CardTitle>
        </CardHeader>
        <CardBody>
          <DescriptionList>
            <DescriptionListGroup>
              <DescriptionListTerm>RUC</DescriptionListTerm>
              <DescriptionListDescription>
                {company?.ruc}
              </DescriptionListDescription>
            </DescriptionListGroup>
            <DescriptionListGroup>
              <DescriptionListTerm>{t("terms.name")}</DescriptionListTerm>
              <DescriptionListDescription>
                {company?.name}
              </DescriptionListDescription>
            </DescriptionListGroup>
            <DescriptionListGroup>
              <DescriptionListTerm>
                {t("terms.description")}
              </DescriptionListTerm>
              <DescriptionListDescription>
                {company?.description}
              </DescriptionListDescription>
            </DescriptionListGroup>
          </DescriptionList>
        </CardBody>
      </Card>

      {modal.isOpen && (
        <Modal
          variant="medium"
          title={t("actions.edit-object", { what: t("terms.company") })}
          isOpen={modal.isOpen}
          onClose={modal.close}
        >
          {company && (
            <GeneralForm
              project={project}
              company={company}
              onSaved={modal.close}
              onCancel={modal.close}
            />
          )}
        </Modal>
      )}
    </>
  );
};