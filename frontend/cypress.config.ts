import { defineConfig } from 'cypress';

export default defineConfig({
  allowCypressEnv: false,

  e2e: {
    baseUrl: 'http://localhost:4200',

    // Répertoire des tests E2E
    specPattern: 'cypress/e2e/**/*.cy.ts',

    // Fichier de support (chargé avant chaque spec)
    supportFile: 'cypress/support/e2e.ts',

    // Dossier des fixtures
    fixturesFolder: 'cypress/fixtures',

    // Timeouts
    defaultCommandTimeout: 8000,
    pageLoadTimeout: 30000,

    setupNodeEvents(on, config) {
      // Plugins Cypress à ajouter ici si besoin
    },
  },
});
