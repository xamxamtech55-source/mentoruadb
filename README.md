# Mentor-UADB

Application desktop de mise en relation et de gestion du mentorat étudiant à l'Université Alioune Diop de Bambey (UADB).

**Stack** : Java 21 • JavaFX 21 • FXML • CSS • JDBC • MySQL 8 (Connector/J 8.3.0) • Maven 3.9+
**Architecture** : JavaFX/FXML/CSS → Controllers → Services → DAO → JDBC → MySQL
**Contexte** : projet de cours « Java avancé » (UADB).

---

## 1. Fonctionnalités

| Écran (FXML) | Contrôleur | Accès |
| --- | --- | --- |
| Accueil animé | `bienvenue` · `BienvenueController` | Tout visiteur (photo d'étudiants à la bibliothèque, effet Ken Burns, cartes interactives) |
| Connexion | `login` · `LoginController` | Tout visiteur (email institutionnel, affichage masqué/visible du mot de passe) |
| Inscription | `inscription` · `InscriptionController` | Tout visiteur (identité, email institutionnel `@uadb.edu.sn`, email de récupération facultatif, UFR/filière/niveau) |
| Mot de passe oublié | `mot-de-passe-oublie` · `MotDePasseOublieController` | Tout visiteur (email institutionnel + n° de carte + adresse de réception du code) |
| Code de réinitialisation | `reinitialisation-code` · `ReinitialisationCodeController` | Tout visiteur (code à 6 chiffres + nouveau mot de passe) |
| Dashboard étudiant | `dashboard-etudiant` · `DashboardEtudiantController` | Étudiant (onglets : Mes demandes, Mes séances, Évaluations, Ressources) |
| Dashboard mentor | `dashboard-mentor` · `DashboardMentorController` | Mentor (onglets : Demandes reçues, Mes séances, Évaluations, Fichiers) |
| Devenir mentor | `devenir-mentor` · `DevenirMentorController` | Étudiant (biographie, expérience, mode, capacité, candidature) |
| Recherche de mentor | `recherche-mentor` · `RechercheMentorController` | Étudiant |
| Liste des mentors | `liste-mentors` · `ListeMentorsController` | Administrateur |
| Séance de groupe | `seance-groupe` · `SeanceGroupeController` | Mentor |
| Mon profil | `profil` · `ProfilController` | Étudiant / Mentor (dont email de récupération) |
| Dashboard admin | `dashboard-admin` · `DashboardAdminController` | Administrateur (menu de boutons) |
| Validation des candidats | `validation-mentors` · `ValidationMentorsController` | Administrateur |
| Gestion des utilisateurs | `gestion-utilisateurs` · `GestionUtilisateursController` | Administrateur |
| Gestion des données | `gestion-donnees` · `GestionDonneesController` | Administrateur (arbre UFR → filières → matières/niveaux) |

**Flux principal** : demande de mentorat (EN_ATTENTE → ACCEPTEE/REFUSEE) → demande de séance → séance (PLANIFIEE/REALISEE/ANNULEE, individuelle ou de groupe) → évaluation de 1 à 5.

**Réinitialisation du mot de passe** : deux écrans. L'utilisateur donne son email institutionnel, son numéro de carte d'étudiant et l'adresse de réception du code (n'importe quel Gmail, pré-rempli avec l'email de récupération du compte quand il existe). Un code à 6 chiffres est envoyé par email (valable 15 minutes, 5 tentatives max) et permet de définir un nouveau mot de passe. En cas de données inconnues ou d'absence d'adresse de réception, le message affiché est le même que pour un envoi réussi : aucune information sur les comptes existants n'est divulguée.

## 2. Prérequis

- JDK 21 ou supérieur
- Maven 3.9+
- MySQL 8 (serveur local ou distant)
- Git

Vérifier les installations :
```bash
java -version
mvn -version
mysql --version
git --version
```

## 3. Mise en place de la base de données

Base vierge :
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/data.sql
```

**Base déjà créée avant une migration ?** Sauvegarde-la (`mysqldump -u root -p mentoruadb > sauvegarde.sql`)
puis exécute les migrations manquantes dans l'ordre (`migration_001`, `_002`, …). Chaque script peut être
relancé sans risque.

Migrations existantes :

| Script | Contenu |
| --- | --- |
| `migration_001_alignement_code.sql` | Alignement du schéma sur le code (renommages, contraintes) |
| `migration_002_partage_fichier_individuel.sql` | Partage de fichiers individuels (mentor → étudiant) |
| `migration_003_demande_seance_notifications.sql` | Demandes de séance et notifications de changement de statut |
| `migration_004_reinitialisation_mot_de_passe.sql` | Table `reinitialisation_mot_de_passe` (code haché, expiration, tentatives) |
| `migration_005_email_recuperation.sql` | Colonne `utilisateur.email_recuperation` |

### Configuration de la connexion

Aucun identifiant n'est écrit dans le code. La connexion se règle, par ordre de priorité :

1. variables d'environnement `MENTORUADB_DB_URL`, `MENTORUADB_DB_USER`, `MENTORUADB_DB_PASSWORD` ;
2. fichier `db.properties` à la racine du projet (copie `db.properties.example` en `db.properties`) —
   ce fichier est ignoré par Git ;
3. valeurs par défaut : MySQL local, utilisateur `root`, sans mot de passe.

Tester la connexion JDBC seule :
```bash
mvn compile exec:java -Dexec.mainClass="com.uadb.mentoruadb.config.DatabaseConnection"
```

## 4. Configuration SMTP (envoi du code de réinitialisation)

L'envoi du mail utilise Gmail (`smtp.gmail.com:587`). Sans configuration, l'application affiche
« Serveur mail non configuré ».

1. copier `mail.properties.example` en `mail.properties` (fichier ignoré par Git) ;
2. créer un **mot de passe d'application** Gmail : activer la validation en 2 étapes
   (`myaccount.google.com/security`), puis `myaccount.google.com/apppasswords` ;
3. renseigner `mail.user`, `mail.password` (les 16 lettres, sans espaces) et `mail.from`.

Alternative : variables d'environnement `MENTORUADB_MAIL_HOST`, `MENTORUADB_MAIL_PORT`, `MENTORUADB_MAIL_USER`,
`MENTORUADB_MAIL_PASSWORD`, `MENTORUADB_MAIL_FROM`.

## 5. Lancer l'application

```bash
mvn clean javafx:run
```

## 6. Lancer les tests

```bash
mvn test
```

## 7. Structure du projet

```
src/main/java/...
  Main.java                    point d'entrée JavaFX (lit bienvenue.fxml)
  config/                      DatabaseConnection (JDBC, config db.properties / env)
  model/                       entités (15 classes, cf. CAHIER_DE_CHARGE § 8.1)
  dao/                         accès aux données (JDBC, PreparedStatement, un DAO par entité)
  dto/                         vues enrichies pour l'affichage (jointures SQL)
  service/                     logique métier et transactions (AuthService, InscriptionService,
                               MentoratService, ReinitialisationService)
  controller/                  contrôleurs FXML (un contrôleur par écran)
  util/                        SceneNavigator, PasswordUtil (PBKDF2), MailUtil (SMTP)
src/main/resources/...
  fxml/                        écrans (.fxml, 16 fichiers)
  css/style.css                styles communs
  images/                      logo UADB, photo d'accueil
src/test/...                   tests unitaires
database/
  schema.sql                   création des tables (fait foi)
  data.sql                     jeu de données de démonstration
  migration_00*.sql            migrations manuelles
README.md / CAHIER_DE_CHARGE.md   documentation
```

## 8. Organisation de l'équipe

| Membre | Responsabilités |
| --- | --- |
| Membre 1 — Chef de projet / Intégrateur | Architecture, GitHub, structure, `Main.java`, navigation, intégration, revue de code, tests globaux |
| Membre 2 — Base de données / JDBC | MEA, MLD, `schema.sql`, `data.sql`, `DatabaseConnection.java`, requêtes JDBC |
| Membre 3 — Interface JavaFX | FXML, CSS, écrans, dashboards, formulaires, ergonomie |
| Membre 4 — Logique métier / Controllers | Controllers, Services, authentification, rôles, demandes, séances, évaluations |

## 9. Workflow Git / GitHub

Un seul dépôt. La branche `main` ne contient que du code stable.
Chaque fonctionnalité travaille sur une branche dédiée, jamais directement sur `main` :

- `feature/dashboard-etudiant-organisation`
- `feature/reinitialisation-email`
- `feature/recherche-mentor`
- `feature/docs-organisation`
- …

**Processus** : création de branche → développement → test → commit → push → Pull Request → revue → validation → fusion dans `main`.

**Une fonctionnalité est validée quand** :
1. Le code compile.
2. La fonctionnalité fonctionne.
3. Elle communique correctement avec MySQL si nécessaire.
4. Les erreurs principales sont gérées.
5. Elle a été testée manuellement.
6. Le code est poussé sur GitHub.
7. La Pull Request a été relue par un autre membre.
8. Elle est fusionnée dans `main`.

## 10. Planning (4 semaines)

| Semaine | Travail |
| --- | --- |
| 1 | Finaliser le MEA, déduire le MLD, créer la base MySQL, créer le projet Java/JavaFX, tester JDBC |
| 2 | Authentification, inscription, utilisateurs, UFR, filières, niveaux, matières |
| 3 | Recherche de mentor, demandes, validation/refus, séances |
| 4 | Évaluation, corrections, tests globaux, préparation démonstration et rapport |

## 11. Sécurité et limites connues

Sécurité :

- Les mots de passe sont stockés hachés (PBKDF2-HMAC-SHA256 avec sel, voir `util/PasswordUtil.java`).
  Les comptes de `data.sql` sont en clair dans le script SQL : convertis en hachage à leur première connexion.
- 100 % des requêtes utilisent `PreparedStatement` ; aucun assemblage de chaînes SQL.
- Identifiants de connexion (BDD, SMTP) et mots de passe d'application jamais dans le dépôt
  (`db.properties`, `mail.properties`, variables d'environnement).
- La réinitialisation du mot de passe vérifie l'identité (email institutionnel + n° de carte), envoie un code
  à 6 chiffres (haché, expiration 15 min, 5 tentatives max, usage unique) et ne révèle rien sur les comptes existants.

Limites connues (assumées) :

- `nombre_max_mentores` est informatif : le mentor déclare sa capacité, mais le système ne bloque pas
  automatiquement une nouvelle demande au-delà du plafond (règle RG22, voir cahier des charges).
- La règle RG20 (Licence 1 non mentor) n'est appliquée que côté interface (bouton masqué) ; elle n'est pas
  encore vérifiée côté service.
- Coverage de tests automatisés limitée : 6 tests unitaires (`PasswordUtilTest`). Les DAO, services et
  contrôleurs ont été validés manuellement.
- Les notifications (y compris de changement de statut) sont internes à l'application (table `notification`) ;
  les emails ne servent qu'à la réinitialisation du mot de passe.
- Les séances `EN_LIGNE` reposent sur un lien fourni par le mentor (pas de visioconférence intégrée).
- Dossier `uploads/` (fichiers partagés) : contenu runtime non versionné.

---

Projet réalisé dans le cadre du cours de Java avancé — UADB.