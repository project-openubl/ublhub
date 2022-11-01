import React, { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

import {
  NotificationContext,
  useConfirmationContext,
  useModal,
} from "@project-openubl/lib-ui";

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
  Hint,
  HintBody,
  HintFooter,
  HintTitle,
  Modal,
} from "@patternfly/react-core";

import { useCompaniesQuery, useUpdateCompanyMutation } from "queries/companies";
import { CompanyDto, ProjectDto } from "api/models";
import { SunatForm } from "./sunat-form";

interface ISunatProps {
  project: ProjectDto;
  company: CompanyDto;
}

export const Sunat: React.FC<ISunatProps> = ({
  project,
  company: companyInput,
}) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const companiesQuery = useCompaniesQuery(project.id || null);
  const company = useMemo(() => {
    return companiesQuery.data?.find((e) => e.id === companyInput.id);
  }, [companyInput, companiesQuery.data]);

  const modal = useModal<"EDIT", CompanyDto>();
  const confirmationModal = useConfirmationContext();
  const { pushNotification } = React.useContext(NotificationContext);

  const updateCompanyMutation = useUpdateCompanyMutation(
    project.id || null,
    (p) => {
      pushNotification({
        title: t("info.data-saved"),
        message: "",
        key: p.name,
        variant: "success",
        actionClose: true,
        timeout: 4000,
      });

      confirmationModal.close();
    }
  );

  const deleteSunatData = () => {
    if (!company) {
      return;
    }

    confirmationModal.open({
      title: "Eliminar datos SUNAT para esta empresa",
      titleIconVariant: "warning",
      message: (
        <div>
          ¿Estas seguro de querer eliminar los datos <b>SUNAT</b> de esta
          empresa?.
        </div>
      ),
      confirmBtnVariant: ButtonVariant.danger,
      confirmBtnLabel: t("actions.delete"),
      cancelBtnLabel: t("actions.cancel"),
      onConfirm: () => {
        confirmationModal.enableProcessing();

        updateCompanyMutation.mutate({
          ...company,
          sunat: undefined,
        });
      },
    });
  };
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
            {company?.sunat && (
              <Button
                variant={ButtonVariant.secondary}
                onClick={deleteSunatData}
              >
                {t("actions.delete")}
              </Button>
            )}
          </CardActions>
          <CardTitle>Preview</CardTitle>
        </CardHeader>
        <CardBody>
          {company?.sunat ? (
            <DescriptionList>
              <DescriptionListGroup>
                <DescriptionListTerm>Factura</DescriptionListTerm>
                <DescriptionListDescription>
                  {company.sunat?.facturaUrl}
                </DescriptionListDescription>
              </DescriptionListGroup>
              <DescriptionListGroup>
                <DescriptionListTerm>Guía</DescriptionListTerm>
                <DescriptionListDescription>
                  {company.sunat?.guiaUrl}
                </DescriptionListDescription>
              </DescriptionListGroup>
              <DescriptionListGroup>
                <DescriptionListTerm>Retención</DescriptionListTerm>
                <DescriptionListDescription>
                  {company.sunat?.retencionUrl}
                </DescriptionListDescription>
              </DescriptionListGroup>
            </DescriptionList>
          ) : (
            <Hint>
              <HintTitle>Sin personalización</HintTitle>
              <HintBody>
                Se utilizará los datos de SUNAT existenten en el "Projecto"
                padre.
              </HintBody>
              <HintFooter>
                <Button
                  variant="link"
                  isInline
                  onClick={() => navigate("../sunat")}
                >
                  Ver configuración en uso
                </Button>
              </HintFooter>
            </Hint>
          )}
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
            <SunatForm
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
