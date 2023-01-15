/// <reference types="cypress" />

import { ProjectsPage } from "../../models/project";
import { DocumentsPage } from "../../models/document";

describe("Flows", () => {
  const projectsPage = new ProjectsPage();
  const documentsPage = new DocumentsPage();

  it("Create project and document", () => {
    const projectName = "my-project";

    // Create project
    projectsPage.create({
      name: projectName,
      facturaUrl:
        "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService",
      guiaUrl:
        "https://api-cpe.sunat.gob.pe/v1/contribuyente/gem",
      retencionUrl:
        "https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService",
      sunatUser: "MODDATOS",
      sunatPassword: "MODDATOS",
    });

    // Create invoice
    documentsPage.create(projectName, "Invoice");

    // Verify table
    cy.get(".pf-c-table")
      .pf4_table_rows()
      .eq(0)
      .find("td[data-label='SUNAT']")
      .contains("ACEPTADO", { timeout: 20_000 });
  });
});
