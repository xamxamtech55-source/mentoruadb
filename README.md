# Mentor-UADB

Application desktop de mise en relation et de gestion du mentorat étudiant à l'Université Alioune Diop de Bambey (UADB).

**Stack** : Java 21 • JavaFX 21 • FXML • CSS • JDBC • MySQL 8
**Architecture** : JavaFX/FXML/CSS → Controllers → Services → DAO → JDBC → MySQL

---

## 1. Prérequis

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

## 2. Mise en place de la base de données

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/data.sql
```

**Base déjà créée avant la migration 001 ?** Sauvegarde-la (`mysqldump -u root -p mentoruadb > sauvegarde.sql`)
puis exécute une seule fois (le script peut être relancé sans risque) :

```bash
mysql -u root -p < database/migration_001_alignement_code.sql
```

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

## 3. Lancer l'application

```bash
mvn clean javafx:run
```

## 3 bis. Lancer les tests

```bash
mvn test
```

## 4. Structure du projet

```
src/main/java/.../Main.java              -> point d'entrée JavaFX
src/main/java/.../config/                -> DatabaseConnection (JDBC)
src/main/java/.../model/                 -> entités (11 classes, cf. MEA section 6)
src/main/java/.../dao/                   -> accès aux données (JDBC, une classe par entité)
src/main/java/.../dto/                   -> vues enrichies pour l'affichage (jointures SQL)
src/main/java/.../service/               -> logique métier (InscriptionService, MentoratService)
src/main/java/.../controller/            -> contrôleurs FXML
src/main/java/.../util/                  -> SceneNavigator (navigation entre écrans)
src/main/resources/.../fxml/             -> écrans (.fxml)
src/main/resources/.../css/              -> styles
database/schema.sql                      -> création des tables
database/data.sql                        -> jeu de données de test                   
```

## 5. Organisation de l'équipe

| Membre | Responsabilités |
| --- | --- |
| Membre 1 — Chef de projet / Intégrateur | Architecture, GitHub, structure, `Main.java`, navigation, intégration, revue de code, tests globaux |
| Membre 2 — Base de données / JDBC | MEA, MLD, `schema.sql`, `data.sql`, `DatabaseConnection.java`, requêtes JDBC |
| Membre 3 — Interface JavaFX | FXML, CSS, écrans, dashboards, formulaires, ergonomie |
| Membre 4 — Logique métier / Controllers | Controllers, Services, authentification, rôles, demandes, séances, évaluations |

## 6. Workflow Git / GitHub

Un seul dépôt. La branche `main` ne contient que du code stable.
Chaque membre travaille sur une branche dédiée à sa fonctionnalité :

- `feature/login`
- `feature/inscription`
- `feature/database`
- `feature/recherche-mentor`
- `feature/demande-mentorat`
- `feature/seance`
- `feature/evaluation`

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

## 7. Planning (4 semaines)

| Semaine | Travail |
| --- | --- |
| 1 | Finaliser le MEA, déduire le MLD, créer la base MySQL, créer le projet Java/JavaFX, tester JDBC |
| 2 | Authentification, inscription, utilisateurs, UFR, filières, niveaux, matières |
| 3 | Recherche de mentor, demandes, validation/refus, séances |
| 4 | Évaluation, corrections, tests globaux, préparation démonstration et rapport |

## 8. Sécurité et limites connues

- Les mots de passe sont stockés hachés (PBKDF2-HMAC-SHA256 avec sel, voir `util/PasswordUtil.java`).
  Les comptes de test de `data.sql` sont en clair dans le fichier SQL : ils sont convertis en hash à leur
  première connexion.
- La réinitialisation du mot de passe (`MotDePasseOublieController`) ne vérifie pas l'identité de la personne :
  il suffit de connaître l'email institutionnel. Une vraie version enverrait un lien ou un code par email.
- `InscriptionService` n'est pas transactionnel : si la création du profil échoue après celle du compte,
  le compte reste en base.

---

Projet réalisé dans le cadre du cours de Java avancé — UADB.
