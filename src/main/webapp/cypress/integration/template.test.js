/// <reference types="cypress" />

context("Test template", () => {
  it("Action buttons disabled when form is invalid", () => {
    cy.visit("/");

    cy.get("#aboutButton").click();
    cy.get(".pf-c-about-modal-box__body").contains("About");
    cy.get("button[aria-label='Close Dialog']").click();
  });
});
