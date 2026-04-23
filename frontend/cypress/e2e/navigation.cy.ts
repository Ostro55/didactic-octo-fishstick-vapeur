// cypress/e2e/navigation.cy.ts
// Tests E2E — Routage, guards et navigation entre pages

describe('Navigation & Auth Guard', () => {

  beforeEach(() => cy.logout());

  // ════════════════════════════════════════════════════════════════════════════
  // Routes publiques — accessibles sans connexion
  // ════════════════════════════════════════════════════════════════════════════
  describe('Routes publiques', () => {

    it.skip('/ est accessible sans connexion', () => {
      cy.mockGamesApi();
      cy.visit('/');
      cy.url().should('eq', Cypress.config().baseUrl + '/');
    });

    it('/login est accessible sans connexion', () => {
      cy.visit('/login');
      cy.url().should('include', '/login');
    });

    it('/search est accessible sans connexion', () => {
      cy.intercept('GET', '**/games*', []).as('getGames');
      cy.visit('/search');
      cy.url().should('include', '/search');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Routes protégées — bloquées sans connexion
  // ════════════════════════════════════════════════════════════════════════════
  describe('Routes protégées — sans connexion', () => {

    it('/admin est bloqué et redirige', () => {
      cy.visit('/admin');
      cy.url().should('not.include', '/admin');
    });

    it('/user est bloqué et redirige', () => {
      cy.visit('/user');
      cy.url().should('not.include', '/user');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Routes protégées — accessibles avec connexion
  // ════════════════════════════════════════════════════════════════════════════
  describe('Routes protégées — avec connexion admin', () => {

    beforeEach(() => {
      cy.loginAs('admin');
      cy.intercept('GET', '**/games*', []).as('getGames');
      cy.intercept('GET', '/users', { fixture: 'users.json' }).as('getUsers');
    });

    it('/admin est accessible pour un admin', () => {
      cy.visit('/admin');
      cy.url().should('include', '/admin');
    });

  });

  describe('Routes protégées — avec connexion utilisateur standard', () => {

    beforeEach(() => {
      cy.loginAs('user');
      cy.intercept('GET', '**/games*', []).as('getGames');
    });

    it('/user est accessible pour un utilisateur connecté', () => {
      cy.visit('/user');
      cy.url().should('include', '/user');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Redirection de la route wildcard
  // ════════════════════════════════════════════════════════════════════════════
  describe('Route wildcard (**)', () => {

    it.skip('redirige une URL inconnue vers /', () => {
      cy.mockGamesApi();
      cy.visit('/cette-page-nexiste-pas');
      cy.url().should('eq', Cypress.config().baseUrl + '/');
    });

    it.skip('redirige /foo/bar vers /', () => {
      cy.mockGamesApi();
      cy.visit('/foo/bar');
      cy.url().should('eq', Cypress.config().baseUrl + '/');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Navigation via la navbar
  // ════════════════════════════════════════════════════════════════════════════
  describe('Navigation via la barre de navigation', () => {

    beforeEach(() => {
      cy.mockGamesApi();
      cy.visit('/');
      cy.wait('@getGames');
    });

    it.skip('le logo Vapeur renvoie vers /', () => {
      cy.get('a.brand-link').click();
      cy.url().should('eq', Cypress.config().baseUrl + '/');
    });

    it.skip('l\'icône compte renvoie vers /login', () => {
      cy.get('a.account-link').click();
      cy.url().should('include', '/login');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Persistance de la session après rechargement
  // ════════════════════════════════════════════════════════════════════════════
  describe('Persistance de la session', () => {

    it('l\'utilisateur reste connecté après un F5', () => {
      cy.loginAs('admin');
      cy.intercept('GET', '**/games*', []).as('getGames');
      cy.visit('/admin');
      cy.reload();
      // Après rechargement, le guard re-lit le localStorage → page toujours accessible
      cy.url().should('include', '/admin');
    });

    it.skip('perd la session après logout puis rechargement', () => {
      cy.loginAs('admin');
      cy.intercept('GET', '**/games*', []).as('getGames');
      cy.intercept('GET', '/users', { fixture: 'users.json' }).as('getUsers');
      cy.visit('/admin');

      cy.mockGamesApi();
      cy.get('.logout-btn').click();
      cy.clearLocalStorage();

      cy.visit('/admin');
      cy.url().should('not.include', '/admin');
    });

  });

});
