/// <reference types="cypress" />

declare global {
    namespace Cypress {
        interface Chainable<Subject> {
            pf4_chip_group_chips(): Chainable<any>;
            pf4_pagination_goToPage(pageNumer: number): Chainable<any>;
            pf4_table_rows(): Chainable<any>;
            pf4_table_row_check(rowIndex: number): Chainable<any>;
            pf4_table_row_edit(
                rowIndex: number,
                action: "open" | "save" | "cancel"
            ): Chainable<any>;
            pf4_table_action_select(
                rowIndex: number,
                actionName: string
            ): Chainable<any>;
            pf4_table_row_expand(rowIndex: number): Chainable<any>;
            pf4_table_column_toggle(column: string): Chainable<any>;
            pf4_table_column_isAsc(column: string): Chainable<any>;
            pf4_table_column_isDesc(column: string): Chainable<any>;
            pf4_dropdown(method: "toggle" | "select", eq?: number): Chainable<any>;
        }
    }
}

// Chips

Cypress.Commands.add(
    "pf4_chip_group_chips",
    { prevSubject: "element" },
    (chipGroup) => {
        return cy.wrap(chipGroup).find(".pf-c-chip-group__list-item");
    }
);

// Pagination

Cypress.Commands.add(
    "pf4_pagination_goToPage",
    { prevSubject: "element" },
    (container, pageNumber) => {
        cy.wrap(container)
            .find(
                ".pf-c-pagination__nav > .pf-c-pagination__nav-page-select > input[aria-label='Current page']"
            )
            .clear()
            .type(`${pageNumber}`)
            .type("{enter}");
    }
);

// Table

/**
 * Using cy.wait() to avoid race conditions. This is a workaround
 * until https://github.com/cypress-io/cypress/issues/7306 is fixed.
 */
Cypress.Commands.add("pf4_table_rows", { prevSubject: "element" }, (table) => {
    cy.wait(250);
    return cy
        .wrap(table)
        .find("tbody > tr")
        .not(".pf-m-expanded")
        .not(".pf-c-table__expandable-row");
});

Cypress.Commands.add(
    "pf4_table_row_check",
    { prevSubject: "element" },
    (table, rowIndex) => {
        cy.wrap(table)
            .find("tbody > tr > td.pf-c-table__check > input")
            .eq(rowIndex)
            .check();
    }
);

Cypress.Commands.add(
    "pf4_table_row_edit",
    { prevSubject: "element" },
    (table, rowIndex, action) => {
        switch (action) {
            case "open":
                cy.wrap(table)
                    .find("tbody > tr > td.pf-c-table__inline-edit-action")
                    .eq(rowIndex)
                    .find("button")
                    .click();
                break;
            case "save":
                cy.wrap(table)
                    .find("tbody > tr > td.pf-c-table__inline-edit-action")
                    .eq(rowIndex)
                    .find("button")
                    .eq(0)
                    .click();
                break;
            case "cancel":
                cy.wrap(table)
                    .find("tbody > tr > td.pf-c-table__inline-edit-action")
                    .eq(rowIndex)
                    .find("button")
                    .eq(1)
                    .click();
            default:
                break;
        }
    }
);

Cypress.Commands.add(
    "pf4_table_action_select",
    { prevSubject: "element" },
    (table, rowIndex, actionName) => {
        cy.wrap(table)
            .find("tbody > tr > td.pf-c-table__action button.pf-c-dropdown__toggle")
            .eq(rowIndex)
            .click();

        cy.wrap(table)
            .find("tbody > tr > td.pf-c-table__action > .pf-c-dropdown > ul > li")
            .contains(actionName)
            .click();
    }
);

Cypress.Commands.add(
    "pf4_table_row_expand",
    { prevSubject: "element" },
    (table, rowIndex) => {
        cy.wrap(table)
            .find("tbody > tr > td.pf-c-table__toggle > button")
            .eq(rowIndex)
            .click();
    }
);

Cypress.Commands.add(
    "pf4_table_column_toggle",
    { prevSubject: "element" },
    (table, column) => {
        cy.wrap(table)
            .find("thead > tr > th.pf-c-table__sort")
            .contains(column)
            .click();
    }
);

Cypress.Commands.add(
    "pf4_table_column_isAsc",
    { prevSubject: "element" },
    (table, column) => {
        cy.wrap(table)
            .find("thead > tr > th.pf-c-table__sort.pf-m-selected")
            .should("have.attr", "aria-sort", "ascending")
            .get("button")
            .contains(column);
    }
);

Cypress.Commands.add(
    "pf4_table_column_isDesc",
    { prevSubject: "element" },
    (table, column) => {
        cy.wrap(table)
            .find("thead > tr > th.pf-c-table__sort.pf-m-selected")
            .should("have.attr", "aria-sort", "descending")
            .get("button")
            .contains(column);
    }
);

// Dropdown

Cypress.Commands.add(
    "pf4_dropdown",
    { prevSubject: "element" },
    (dropdown, method, eq) => {
        switch (method) {
            case "toggle":
                return cy.wrap(dropdown).find("button.pf-c-dropdown__toggle").click();
            case "select":
                return cy.wrap(dropdown).find("ul > li").eq(eq);

            default:
                break;
        }
    }
);

export {};
