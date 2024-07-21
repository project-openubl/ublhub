import React, { useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useModal } from "@project-openubl/lib-ui";

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

import { useCompaniesQuery } from "queries/companies";

import { CompanyDto, ProjectDto } from "api/models";
import { GeneralForm } from "./general-form";
import { CompanyLogo } from "shared/components/company-logo";

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

  const companiesQuery = useCompaniesQuery(project.name || null);
  const company = useMemo(() => {
    return companiesQuery.data?.find((e) => e.ruc === companyInput.ruc);
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
              <DescriptionListTerm>Logo</DescriptionListTerm>
              <DescriptionListDescription>
                {project?.name && company?.ruc && (
                  <CompanyLogo projectName={project.name} companyRuc={company.ruc} />
                )}
              </DescriptionListDescription>
            </DescriptionListGroup>
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
          title={t("actions.edit-object", {
            what: t("terms.company").toLowerCase(),
          })}
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
