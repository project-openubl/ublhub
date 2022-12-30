/// <reference types="cypress" />

export interface IFormValue {
  name: string;
  description?: string;
  facturaUrl: string;
  guiaUrl: string;
  retencionUrl: string;
  sunatUser: string;
  sunatPassword: string;
}

export class ProjectsPage {
  openPage(): void {
    // Interceptors
    cy.intercept("POST", "/api/projects").as("postProject");
    cy.intercept("GET", "/api/projects").as("getProjects");

    // Open page
    cy.visit("#/projects");
    cy.wait("@getProjects");
  }

  create(formValue: IFormValue): void {
    this.openPage();

    // Open modal
    cy.get("button[aria-label='new-project']").click();

    // Fill form
    cy.get("input[name='name']").clear().type(formValue.name);
    if (formValue.description) {
      cy.get("textarea[name='name']").clear().type(formValue.description);
    }

    cy.get("button").contains("Siguiente").click();

    cy.get("input[name='factura']").clear().type(formValue.facturaUrl);
    cy.get("input[name='guia']").clear().type(formValue.guiaUrl);
    cy.get("input[name='retencion']").clear().type(formValue.retencionUrl);

    cy.get("button").contains("Siguiente").click();

    cy.get("input[name='username']").clear().type(formValue.sunatUser);
    cy.get("input[name='password']").clear().type(formValue.sunatPassword);

    cy.get("button").contains("Siguiente").click();
    cy.get("button").contains(/^Crear$/).click();

    // Wait for listeners
    cy.wait("@postProject");
    cy.wait("@getProjects");
  }
}
