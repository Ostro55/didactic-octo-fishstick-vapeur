// cypress/e2e/home.cy.ts
// Tests E2E complets — Page d'accueil (/)

describe('Page d\'accueil', () => {

  // ════════════════════════════════════════════════════════════════════════════
  // Setup commun
  // ════════════════════════════════════════════════════════════════════════════
  beforeEach(() => {
    cy.logout();
    cy.mockGamesApi();
    cy.visit('/');
  });

  // ════════════════════════════════════════════════════════════════════════════
  // Navbar
  // ════════════════════════════════════════════════════════════════════════════
  describe('Barre de navigation', () => {

    it('affiche le logo "Vapeur"', () => {
      cy.get('.brand-link').should('be.visible');
      cy.get('.brand-text').should('contain', 'Vapeur');
    });

    it('affiche l\'image de marque', () => {
      cy.get('.brand-icon').should('be.visible').and('have.attr', 'alt', 'Home');
    });

    it('le logo renvoie vers la page d\'accueil', () => {
      cy.get('a.brand-link').should('have.attr', 'href', '/');
    });

    it('affiche le champ de recherche', () => {
      cy.get('.search-form input[name="search"]')
        .should('be.visible')
        .and('have.attr', 'placeholder', 'Search the store');
    });

    it('affiche le bouton Search', () => {
      cy.get('.search-form button[type="submit"]')
        .should('be.visible')
        .and('contain', 'Search');
    });

    it('affiche le lien vers la page compte', () => {
      cy.get('a.account-link').should('have.attr', 'href', '/login');
    });

    it('affiche l\'icône de compte', () => {
      cy.get('.account-icon').should('be.visible').and('have.attr', 'alt', 'Account');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Chargement des jeux
  // ════════════════════════════════════════════════════════════════════════════
  describe('Chargement des jeux', () => {

    it.skip('appelle l\'API games au chargement de la page', () => {
      cy.wait('@getGames');
    });

    it.skip('affiche la section des résultats', () => {
      cy.wait('@getGames');
      cy.get('.results-container').should('be.visible');
    });

    it.skip('affiche le titre "Résultats de recherche"', () => {
      cy.wait('@getGames');
      cy.get('.results-title').should('contain', 'Résultats de recherche');
    });

    it.skip('affiche autant de cartes que de jeux retournés par l\'API', () => {
      cy.wait('@getGames');
      cy.get('.result-card').should('have.length', 3);
    });

    it('affiche le nom du premier jeu', () => {
      cy.wait('@getGames');
      cy.get('.result-card').first().should('contain', 'Super Mario Bros');
    });

    it('affiche le prix du premier jeu', () => {
      cy.wait('@getGames');
      cy.get('.result-card').first().should('contain', '59.99');
    });

    it('affiche l\'éditeur du premier jeu', () => {
      cy.wait('@getGames');
      cy.get('.result-card').first().should('contain', 'Nintendo');
    });

    it('affiche l\'id du premier jeu', () => {
      cy.wait('@getGames');
      cy.get('.result-card').first().should('contain', '1');
    });

    it.skip('affiche une liste vide si l\'API retourne []', () => {
      cy.intercept('GET', '**/games*', []).as('getGamesEmpty');
      cy.visit('/');
      cy.wait('@getGamesEmpty');
      cy.get('.result-card').should('not.exist');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Overlay — ouverture et fermeture
  // ════════════════════════════════════════════════════════════════════════════
  describe('Overlay de détail', () => {

    beforeEach(() => cy.wait('@getGames'));

    it('l\'overlay est masqué au départ', () => {
      cy.get('.overlay').should('have.css', 'display', 'none');
    });

    it.skip('ouvre l\'overlay au clic sur une carte jeu', () => {
      cy.get('.bordered-div').first().click();
      cy.get('.overlay').should('not.have.css', 'display', 'none');
    });

    it.skip('ferme l\'overlay au clic sur le bouton X', () => {
      cy.get('.bordered-div').first().click();
      cy.get('.overlay button').click();
      cy.get('.overlay').should('have.css', 'display', 'none');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Bouton "add a game" — visible seulement si connecté
  // ════════════════════════════════════════════════════════════════════════════
  describe('Bouton "add a game"', () => {

    it('n\'affiche pas le bouton "add a game" si non connecté', () => {
      cy.get('button').filter(':contains("add a game")').should('not.exist');
    });

    it.skip('affiche le bouton "add a game" quand l\'utilisateur est connecté', () => {
      cy.loginAs('user');
      cy.visit('/');
      cy.mockGamesApi();
      cy.get('button').filter(':contains("add a game")').should('be.visible');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Recherche via la navbar
  // ════════════════════════════════════════════════════════════════════════════
  describe('Recherche via la navbar', () => {

    it.skip('permet de taper dans le champ de recherche', () => {
      cy.get('.search-form input[name="search"]')
        .type('mario')
        .should('have.value', 'mario');
    });

    it.skip('soumet le formulaire et navigue vers /search avec le paramètre', () => {
      cy.get('.search-form input[name="search"]').type('mario');
      cy.get('.search-form button[type="submit"]').click();
      cy.url().should('include', '/search').and('include', 'search=mario');
    });

  });

});
