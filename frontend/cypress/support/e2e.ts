// cypress/support/e2e.ts
// Chargé automatiquement avant chaque fichier de test E2E

import './commands';

// Ignorer toutes les erreurs non capturées venant de l'application
// (ex: erreurs RxJS/HttpClient sur requêtes en 404, zone Angular, ResizeObserver…)
Cypress.on('uncaught:exception', () => false);
