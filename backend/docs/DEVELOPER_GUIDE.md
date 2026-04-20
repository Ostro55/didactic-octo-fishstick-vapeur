# Guide Developpeur

Ce document decrit l'organisation du backend, les choix techniques visibles dans le code et les points a connaitre avant de faire evoluer l'application.

## Vue d'ensemble

Le projet suit une structure Spring Boot classique:

- `controller/` expose l'API REST
- `service/` contient la logique metier
- `repository/` encapsule l'acces JPA
- `entity/` declare le modele persistant
- `audit/` gere l'emission d'evenements d'audit
- `src/main/resources/static/` embarque la documentation OpenAPI et Swagger UI

Point d'entree applicatif:

- [BackWorkApplication.java](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/java/com/vapeur/backwork/BackWorkApplication.java)

## Organisation metier

### Jeux

Le service [GameService.java](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/java/com/vapeur/backwork/service/GameService.java) gere:

- la lecture de tous les jeux
- le filtrage par nom, prix et genre
- la creation simple d'un jeu
- la creation d'un jeu rattachee a un utilisateur
- la validation d'un jeu via `acceptGame`
- la purge complete des tables `games`, `game_genres` et `user_recommended_games`

Regle metier importante:

- `POST /games/save?userId=...` ignore le `status` eventuellement envoye par le client
- le statut est force a `accepted` si l'utilisateur est admin
- sinon il est force a `pending`

Le filtrage est implemente dans [GameFilters.java](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/java/com/vapeur/backwork/service/GameFilters.java).

Caracteristiques du filtrage:

- comparaison du nom en egalite stricte, insensible a la casse
- filtrage du genre insensible a la casse
- genre invalide => resultat vide
- filtrage applique en memoire sur `findAll()`

### Utilisateurs

Le service [UserService.java](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/java/com/vapeur/backwork/service/UserService.java) gere:

- CRUD utilisateur
- nettoyage des liaisons `user_recommended_games` avant suppression
- login simple via email + mot de passe

Le login actuel:

- ne cree pas de token
- ne gere ni hash de mot de passe ni autorisation
- renvoie simplement l'utilisateur correspondant ou `404`

## Modele de donnees

### Entite `Game`

Reference:

- [Game.java](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/java/com/vapeur/backwork/entity/Game.java)

Champs principaux:

- `status` vaut `pending` par defaut
- `genre` est stocke dans une table de collection `game_genres`
- `release_date` est un `Timestamp`

### Entite `User`

Reference:

- [User.java](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/java/com/vapeur/backwork/entity/User.java)

Points techniques:

- `username` et `email` sont uniques
- `recommendedGames` est une relation `@ManyToMany` chargee en `EAGER`
- la propriete JSON exposee cote API est `isAdmin`

## Audit Kafka

Le sous-package `audit/` publie des evenements sur Kafka quand `audit.kafka.enabled=true`.

Composants principaux:

- [AuditEventPublisher.java](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/java/com/vapeur/backwork/audit/AuditEventPublisher.java)
- [KafkaAuditEventPublisher.java](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/java/com/vapeur/backwork/audit/KafkaAuditEventPublisher.java)
- [NoopAuditEventPublisher.java](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/java/com/vapeur/backwork/audit/NoopAuditEventPublisher.java)

Comportement:

- sans profil Kafka, l'implementation `Noop` absorbe les evenements
- avec Kafka actif, les evenements sont serialises en JSON puis envoyes sur le topic configure
- quand une transaction Spring est active, l'envoi est differe apres le commit

Variables de configuration:

- `AUDIT_KAFKA_ENABLED`
- `AUDIT_TOPIC`
- `KAFKA_BOOTSTRAP_SERVERS`

## Configuration et environnements

Fichiers de configuration:

- [application.properties](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/resources/application.properties)
- [application-h2.properties](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/resources/application-h2.properties)
- [application-postgres.properties](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/resources/application-postgres.properties)
- [application-kafka.properties](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/resources/application-kafka.properties)

Regles actuelles:

- le profil par defaut est `postgres`
- `spring.jpa.hibernate.ddl-auto` vaut `update` par defaut
- `spring.jpa.open-in-view=false`
- la console H2 est active avec le profil `h2`

## Demarrage developpeur

### Local minimal

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

### Local proche integration

```bash
docker compose up --build
```

Le compose fournit:

- PostgreSQL 16
- Kafka 3.7.2
- l'application avec profils `postgres,kafka`

## Tests

Les tests se trouvent dans `src/test/java`.

Couverture actuelle visible:

- tests de controleurs `GameControllerTest` et `UserControllerTest`
- tests du filtrage de jeux
- tests des services
- tests d'integration de l'audit Kafka

Commande de base:

```bash
./mvnw test
```

Les tests de controleurs utilisent principalement le profil `h2` avec `ddl-auto=create-drop`.

## Documentation API embarquee

La documentation statique est servie depuis:

- [openapi.yaml](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/resources/static/openapi.yaml)
- [swagger-ui.html](/home/epita/dev/projFullStack/didactic-octo-fishstick-vapeur/backend/src/main/resources/static/swagger-ui.html)

Si vous ajoutez ou modifiez un endpoint:

1. mettez a jour le controller et le service
2. adaptez ou ajoutez les tests
3. synchronisez `openapi.yaml`
4. verifiez que les exemples de `README.md` restent valides

## Points de vigilance avant evolution

- plusieurs endpoints retournent `500` lorsqu'un `Optional` est vide; si vous corrigez cela en `404`, verifiez l'impact front et tests
- le filtrage des jeux en memoire peut devenir couteux avec beaucoup de donnees; une migration vers des requetes JPA/specifications serait plus robuste
- les suppressions globales passent par des requetes SQL natives pour respecter l'ordre impose par les cles etrangeres
- `recommendedGames` et `genre` sont charges en `EAGER`, ce qui simplifie l'API mais peut alourdir certaines lectures
- le login actuel ne doit pas etre considere comme un mecanisme de securite de production

## Suggestions d'amelioration

- introduire des DTO de reponse et de requete pour dissocier l'API des entites JPA
- remplacer le login en clair par Spring Security + hash de mots de passe
- retourner des codes HTTP plus precis
- deplacer le filtrage vers la couche repository
- ajouter une vraie versionnee de la spec OpenAPI a chaque changement d'API
