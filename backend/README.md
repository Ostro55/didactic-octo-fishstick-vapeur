# Backend Catalogue de Jeux

Ce projet est le backend d'un catalogue de jeux video. Il expose une API REST pour gerer les jeux, gerer les utilisateurs, connecter un utilisateur et publier des evenements d'audit sur Kafka quand cette integration est active.

## Fonctionnalites

- gestion des jeux du catalogue
- filtrage des jeux par nom, genre et fourchette de prix
- creation de jeux avec statut derive du role utilisateur
- gestion des utilisateurs
- endpoint de login simple base sur email + mot de passe
- publication optionnelle d'evenements d'audit dans Kafka
- documentation API via OpenAPI et Swagger UI

## Stack technique

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- H2 pour le developpement rapide
- PostgreSQL pour un environnement proche de la production
- Kafka pour l'audit applicatif
- Maven

## Demarrage rapide

### Prerequis

- Java 17
- Docker et Docker Compose si vous voulez lancer toute la stack

### Option 1: lancer en local avec H2

Le profil `h2` permet de demarrer rapidement sans dependance externe.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

L'API sera disponible sur `http://localhost:8080`.

### Option 2: lancer avec PostgreSQL et Kafka via Docker Compose

```bash
docker compose up --build
```

Cette commande demarre:

- PostgreSQL sur `localhost:5432`
- Kafka sur `localhost:9092`
- l'application sur `localhost:8080`

Le conteneur applicatif active les profils `postgres,kafka`.

## Profils Spring

### `postgres`

Profil par defaut. Il lit la configuration suivante:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Valeurs par defaut:

- `jdbc:postgresql://localhost:5432/back_work`
- `postgres`
- `postgres`

### `h2`

Base en memoire utile pour les tests manuels et le developpement local.

### `kafka`

Active la publication des evenements d'audit vers Kafka. Variables importantes:

- `KAFKA_BOOTSTRAP_SERVERS` par defaut `localhost:9092`
- `AUDIT_TOPIC` par defaut `audit-events`
- `AUDIT_KAFKA_ENABLED`

## Documentation API

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Specification OpenAPI: `http://localhost:8080/openapi.yaml`

## Endpoints principaux

### Jeux

- `GET /games` liste tous les jeux
- `GET /games?name=Doom&genre=action&minPrice=10&maxPrice=50` filtre les jeux
- `GET /games/{id}` recupere un jeu
- `POST /games` cree un jeu sans contexte utilisateur
- `POST /games/save?userId=1` cree un jeu en derivant le statut depuis l'utilisateur
- `PUT /games/{id}/accept` passe un jeu au statut `accepted`
- `DELETE /games` supprime tous les jeux et les tables de jointure associees

Genres supportes:

- `action`
- `romance`
- `thriller`
- `horror`
- `multiplayer`
- `singleplayer`

Exemple de creation de jeu:

```bash
curl -X POST http://localhost:8080/games/save?userId=1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Hades",
    "price": 25,
    "description": "Rogue-like d action",
    "editor": "Supergiant Games",
    "img_url": "https://example.com/hades.jpg",
    "genre": ["action", "singleplayer"]
  }'
```

Comportement metier important:

- si `userId` correspond a un admin, le jeu est cree avec le statut `accepted`
- sinon il est cree avec le statut `pending`
- si le genre fourni dans un filtre est invalide, l'API renvoie une liste vide

### Utilisateurs

- `GET /users` liste tous les utilisateurs
- `GET /users/{id}` recupere un utilisateur
- `POST /users` cree un utilisateur
- `PUT /users/{id}` met a jour un utilisateur
- `DELETE /users/{id}` supprime un utilisateur
- `DELETE /users` supprime tous les utilisateurs et les recommandations associees
- `POST /users/login` effectue un login simple

Exemple de creation d'utilisateur:

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "pw",
    "email": "alice@example.com",
    "isAdmin": false,
    "recommendedGames": []
  }'
```

Exemple de login:

```bash
curl -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "pw"
  }'
```

## Modele de donnees

### `Game`

- `id`
- `name`
- `price`
- `description`
- `release_date`
- `img_url`
- `editor`
- `status`
- `genre`

### `User`

- `id`
- `username`
- `password`
- `email`
- `isAdmin`
- `recommendedGames`

## Limites actuelles

- aucun mecanisme d'authentification JWT ou session n'est implemente
- le login compare le mot de passe en clair
- certains endpoints renvoient actuellement `500` quand la ressource n'existe pas au lieu de `404`
- le filtrage des jeux est realise en memoire apres lecture complete en base

## Documentation developpeur

Le guide de reprise technique se trouve dans [docs/DEVELOPER_GUIDE.md](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/docs/DEVELOPER_GUIDE.md).
