// cypress/e2e/login.cy.ts
// Tests E2E complets — Page de connexion (/login)

describe('Page de connexion', () => {

  beforeEach(() => {
    cy.logout();
    cy.mockUsersApi();
    cy.visit('/login');
  });

  // ════════════════════════════════════════════════════════════════════════════
  // Rendu initial
  // ════════════════════════════════════════════════════════════════════════════
  describe('Rendu initial', () => {

    it('affiche le titre "S\'identifier"', () => {
      cy.contains('h2', "S'identifier").should('be.visible');
    });

    it('affiche un champ email', () => {
      cy.get('input[type="email"]')
        .should('be.visible')
        .and('have.attr', 'placeholder', 'Email');
    });

    it('affiche un champ mot de passe masqué', () => {
      cy.get('input[type="password"]')
        .should('be.visible')
        .and('have.attr', 'placeholder', 'Password');
    });

    it('affiche le bouton "Log in"', () => {
      cy.get('input[type="submit"][value="Log in"]').should('be.visible');
    });

    it('affiche le bouton "Sing up"', () => {
      cy.get('input[type="submit"][value="Sing up"]').should('be.visible');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Saisie dans le formulaire
  // ════════════════════════════════════════════════════════════════════════════
  describe('Saisie utilisateur', () => {

    it('accepte la frappe dans le champ email', () => {
      cy.get('input[type="email"]')
        .type('alice@example.com')
        .should('have.value', 'alice@example.com');
    });

    it('accepte la frappe dans le champ mot de passe', () => {
      cy.get('input[type="password"]')
        .type('monmotdepasse')
        .should('have.value', 'monmotdepasse');
    });

    it('peut effacer et retaper dans les champs', () => {
      cy.get('input[type="email"]').type('erreur@test.com').clear().type('alice@example.com');
      cy.get('input[type="email"]').should('have.value', 'alice@example.com');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Validation — formulaire vide ou incomplet
  // ════════════════════════════════════════════════════════════════════════════
  describe('Validation du formulaire', () => {

    it('ne déclenche pas d\'appel API si les deux champs sont vides', () => {
      cy.get('input[type="submit"][value="Log in"]').click();
      cy.get('@getUsers.all').should('have.length', 0);
    });

    it('ne déclenche pas d\'appel API si seul l\'email est rempli', () => {
      cy.get('input[type="email"]').type('alice@example.com');
      cy.get('input[type="submit"][value="Log in"]').click();
      cy.get('@getUsers.all').should('have.length', 0);
    });

    it('ne déclenche pas d\'appel API si seul le mot de passe est rempli', () => {
      cy.get('input[type="password"]').type('password');
      cy.get('input[type="submit"][value="Log in"]').click();
      cy.get('@getUsers.all').should('have.length', 0);
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Connexion réussie — utilisateur standard
  // ════════════════════════════════════════════════════════════════════════════
  describe('Connexion réussie — utilisateur standard', () => {

    beforeEach(() => {
      cy.get('input[type="email"]').type('alice@example.com');
      cy.get('input[type="password"]').type('password');
      cy.get('input[type="submit"][value="Log in"]').click();
      cy.wait('@getUsers');
    });

    it('appelle l\'API /users lors de la soumission', () => {
      // wait déjà fait dans beforeEach — on vérifie simplement que la requête a eu lieu
      cy.get('@getUsers').should('exist');
    });

    it('stocke l\'email dans le localStorage', () => {
      cy.getAllLocalStorage().then((storage) => {
        const s = Object.values(storage)[0] as Record<string, string>;
        expect(s['User']).to.equal('alice@example.com');
      });
    });

    it('stocke l\'ID utilisateur dans le localStorage', () => {
      cy.getAllLocalStorage().then((storage) => {
        const s = Object.values(storage)[0] as Record<string, string>;
        expect(s['ID']).to.equal('1');
      });
    });

    it('stocke isAdmin=false dans le localStorage', () => {
      cy.getAllLocalStorage().then((storage) => {
        const s = Object.values(storage)[0] as Record<string, string>;
        expect(s['Admin']).to.equal('false');
      });
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Connexion réussie — administrateur
  // ════════════════════════════════════════════════════════════════════════════
  describe('Connexion réussie — administrateur', () => {

    beforeEach(() => {
      cy.get('input[type="email"]').type('admin@example.com');
      cy.get('input[type="password"]').type('admin');
      cy.get('input[type="submit"][value="Log in"]').click();
      cy.wait('@getUsers');
    });

    it('stocke isAdmin=true dans le localStorage', () => {
      cy.getAllLocalStorage().then((storage) => {
        const s = Object.values(storage)[0] as Record<string, string>;
        expect(s['Admin']).to.equal('true');
      });
    });

    it('stocke l\'ID de l\'admin dans le localStorage', () => {
      cy.getAllLocalStorage().then((storage) => {
        const s = Object.values(storage)[0] as Record<string, string>;
        expect(s['ID']).to.equal('2');
      });
    });

    it('redirige vers /admin après la connexion', () => {
      cy.url().should('include', '/admin');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Connexion échouée — email inconnu
  // ════════════════════════════════════════════════════════════════════════════
  describe('Connexion échouée — email inconnu', () => {

    beforeEach(() => {
      cy.get('input[type="email"]').type('inconnu@example.com');
      cy.get('input[type="password"]').type('password');
      cy.get('input[type="submit"][value="Log in"]').click();
      cy.wait('@getUsers');
    });

    it('n\'enregistre pas d\'ID dans le localStorage', () => {
      cy.getAllLocalStorage().then((storage) => {
        const s = (Object.values(storage)[0] ?? {}) as Record<string, string>;
        expect(s['ID']).to.be.undefined;
      });
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Inscription (Sign up)
  // ════════════════════════════════════════════════════════════════════════════
  describe('Inscription', () => {

    it('appelle POST /users au clic sur "Sing up"', () => {
      cy.intercept('POST', '/users', {}).as('createUser');
      cy.mockGamesApi();

      cy.get('input[type="email"]').type('nouveau@example.com');
      cy.get('input[type="password"]').type('secret123');
      cy.get('input[type="submit"][value="Sing up"]').click();

      cy.wait('@createUser').its('request.body').then((body) => {
        expect(body.email).to.equal('nouveau@example.com');
        expect(body.password).to.equal('secret123');
      });
    });

    it.skip('redirige vers / après l\'inscription', () => {
      cy.intercept('POST', '/users', {}).as('createUser');
      cy.mockGamesApi();

      cy.get('input[type="email"]').type('nouveau@example.com');
      cy.get('input[type="password"]').type('secret123');
      cy.get('input[type="submit"][value="Sing up"]').click();

      cy.wait('@createUser');
      cy.url().should('eq', Cypress.config().baseUrl + '/');
    });

  });

  // ════════════════════════════════════════════════════════════════════════════
  // Déjà connecté — redirection automatique
  // ════════════════════════════════════════════════════════════════════════════
  describe('Déjà connecté', () => {

    it('redirige vers /admin si l\'utilisateur est admin', () => {
      cy.loginAs('admin');
      cy.visit('/login');
      cy.url().should('include', '/admin');
    });

    it('redirige vers /user si l\'utilisateur est standard', () => {
      cy.loginAs('user');
      cy.visit('/login');
      cy.url().should('include', '/user');
    });

  });

});
