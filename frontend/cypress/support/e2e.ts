// cypress/support/e2e.ts
// Chargé automatiquement avant chaque fichier de test E2E

import './commands';

// Désactiver les erreurs non capturées qui viendraient des libs tierces
Cypress.on('uncaught:exception', (err) => {
  // Ignorer les erreurs RxJS de souscription hors Angular Zone
  if (err.message.includes('ResizeObserver') || err.message.includes('zone')) {
    return false;
  }
  return true;
});
