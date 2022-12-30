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

export class DocumentsPage {
  openPage(projectName: string): void {
    // Open page
    cy.visit("#/documents");

    // Select project
    cy.get("div[data-ouia-component-id='project-context-selector']").click();
    cy.get("button.pf-c-context-selector__menu-list-item").first().click();
  }

  create(projectName: string, type: string): void {
    this.openPage(projectName);

    // Open modal
    cy.get("button[aria-label='new-document']").click();

    // Fill form
    cy.get("div.pf-c-empty-state__secondary > button").contains(type).click();

    cy.get("div.pf-c-form__actions > button").contains("Guardar").click();
  }
}
