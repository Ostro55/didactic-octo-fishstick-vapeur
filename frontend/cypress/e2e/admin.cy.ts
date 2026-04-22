// cypress/e2e/admin.cy.ts
// Tests E2E complets — Page administrateur (/admin)

describe('Page administrateur', () => {

  // ════════════════════════════════════════════════════════════════════════════
  // Setup commun — connexion admin avant chaque test
  // ════════════════════════════════════════════════════════════════════════════
  beforeEach(() => {
    cy.logout();
    cy.loginAs('admin');
    cy.intercept('GET', '**/games*', []).as('getGames');
    cy.intercept('GET', '/users', { fixture: 'users.json' }).as('getUsers');
    cy.visit('/admin');
  });

  // ════════════════════════════════════════════════════════════════════════════
  // Rendu
  // ════════════════════════════════════════════════════════════════════════════
  describe('Rendu de la page', () => {

    it.skip('affiche le titre "Super Administrateur"', () => {
      cy.contains('h1', 'Super Administrateur').should('be.visible');
    });

    it.skip('affiche l\'email de l\'admin connecté', () => {
      cy.get('.email span').should('contain', 'admin@example.com');
    });

    it.skip('affiche le bouton "Se déconnecter"', () => {
      cy.get('.logout-btn').should('be.visible').and('contain', 'Se déconnecter');
    });

    it.skip('affiche la section pour bannir un utilisateur', () => {
      cy.contains('h2', 'Bannir un utilisateur').should('be.visible');
    });

    it('affiche la section pour débannir un utilisateur', () => {
      cy.contains('h2', 'Débannir un utilisateur').should('be.visible');
    });

    it('affiche le champ de saisie pour bannir', () => {
      cy.get('.admin-grid .admin-card').first()
        .find('input[type="text"]')
        .should('have.attr', 'placeholder', 'ID utilisateur à bannir');
    });

    it('affiche le champ de saisie pour débannir', () => {
      cy.get('.admin-grid .admin-card').last()
        .find('input[type="text"]')
        .should('have.attr', 'placeholder', 'ID utilisateur à débannir');
    });

    it('affiche le bouton "Bannir" rouge', () => {
      cy.get('.danger-btn').should('be.visible').and('contain', 'Bannir');
    });

    it('affiche le bouton "Débannir" vert', () => {
      cy.get('.success-btn').should('be.visible').and('contain', 'Débannir');
    });

    it('affiche la barre de navigation', () => {
      cy.get('app-home-button').should('exist');
      cy.get('.steam-navbar').should('be.visible');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Action — bannir un utilisateur
  // ════════════════════════════════════════════════════════════════════════════
  describe('Bannir un utilisateur', () => {

    it('permet de saisir un ID dans le champ bannir', () => {
      cy.get('.admin-grid .admin-card').first()
        .find('input[type="text"]')
        .type('42')
        .should('have.value', '42');
    });

    it('soumet le formulaire de ban au clic sur "Bannir"', () => {
      cy.get('.admin-grid .admin-card').first()
        .find('input[type="text"]')
        .type('42');
      cy.get('.danger-btn').click();
      // Le formulaire doit rester visible (pas de navigation)
      cy.get('.danger-btn').should('be.visible');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Action — débannir un utilisateur
  // ════════════════════════════════════════════════════════════════════════════
  describe('Débannir un utilisateur', () => {

    it('permet de saisir un ID dans le champ débannir', () => {
      cy.get('.admin-grid .admin-card').last()
        .find('input[type="text"]')
        .type('42')
        .should('have.value', '42');
    });

    it('soumet le formulaire de déban au clic sur "Débannir"', () => {
      cy.get('.admin-grid .admin-card').last()
        .find('input[type="text"]')
        .type('42');
      cy.get('.success-btn').click();
      cy.get('.success-btn').should('be.visible');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Déconnexion
  // ════════════════════════════════════════════════════════════════════════════
  describe('Déconnexion', () => {

    it.skip('supprime les données du localStorage au clic sur "Se déconnecter"', () => {
      cy.mockGamesApi();
      cy.get('.logout-btn').click();

      cy.getAllLocalStorage().then((storage) => {
        const s = (Object.values(storage)[0] ?? {}) as Record<string, string>;
        expect(s['ID']).to.be.undefined;
        expect(s['User']).to.be.undefined;
        expect(s['Admin']).to.be.undefined;
      });
    });

    it.skip('redirige vers /connexion après déconnexion', () => {
      cy.mockGamesApi();
      cy.get('.logout-btn').click();
      cy.url().should('include', '/connexion');
    });

  });

});
