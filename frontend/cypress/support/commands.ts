// cypress/support/commands.ts
// Commandes personnalisées réutilisables dans tous les tests

// ── Authentification ──────────────────────────────────────────────────────────

/**
 * Connecte un utilisateur standard via le localStorage (sans passer par l'UI).
 * Usage : cy.loginAs('user')
 */
Cypress.Commands.add('loginAs', (role: 'user' | 'admin') => {
  if (role === 'admin') {
    cy.window().then((win) => {
      win.localStorage.setItem('User',  'admin@example.com');
      win.localStorage.setItem('ID',    '2');
      win.localStorage.setItem('Admin', 'true');
    });
  } else {
    cy.window().then((win) => {
      win.localStorage.setItem('User',  'alice@example.com');
      win.localStorage.setItem('ID',    '1');
      win.localStorage.setItem('Admin', 'false');
    });
  }
});

/**
 * Connecte un utilisateur via le formulaire de login (test bout-en-bout réel).
 * Usage : cy.loginViaUI('alice@example.com', 'password')
 */
Cypress.Commands.add('loginViaUI', (email: string, password: string) => {
  cy.visit('/login');
  cy.get('input[type="email"]').type(email);
  cy.get('input[type="password"]').type(password);
  cy.get('input[type="submit"][value="Log in"]').click();
});

/**
 * Déconnecte l'utilisateur en nettoyant le localStorage.
 */
Cypress.Commands.add('logout', () => {
  cy.clearLocalStorage();
});

// ── Intercepts réutilisables ──────────────────────────────────────────────────

/**
 * Intercepte et mocke l'API /games avec le fixture games.json.
 * Usage : cy.mockGamesApi()
 */
Cypress.Commands.add('mockGamesApi', () => {
  cy.intercept('GET', '**/games*', { fixture: 'games.json' }).as('getGames');
});

/**
 * Intercepte et mocke l'API /users avec le fixture users.json.
 * Usage : cy.mockUsersApi()
 */
Cypress.Commands.add('mockUsersApi', () => {
  cy.intercept('GET', '/users', { fixture: 'users.json' }).as('getUsers');
});

// ── Déclaration TypeScript ────────────────────────────────────────────────────
declare global {
  namespace Cypress {
    interface Chainable {
      loginAs(role: 'user' | 'admin'): Chainable<void>;
      loginViaUI(email: string, password: string): Chainable<void>;
      logout(): Chainable<void>;
      mockGamesApi(): Chainable<void>;
      mockUsersApi(): Chainable<void>;
    }
  }
}
