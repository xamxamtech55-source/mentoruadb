# MentorUADB

Application desktop de mise en relation et de gestion du mentorat étudiant à l'Université Alioune Diop de Bambey (UADB).

**Stack** : Java 17 • JavaFX 21 • FXML • CSS • JDBC • MySQL 8
**Architecture** : JavaFX/FXML/CSS → Controllers → Services → JDBC → MySQL (pas de couche DAO)

---

## 1. Prérequis

- JDK 17 ou supérieur
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

Puis adapter si besoin les identifiants dans
`src/main/java/com/uadb/mentoruadb/config/DatabaseConnection.java`
(URL, utilisateur, mot de passe).

Tester la connexion JDBC seule :
```bash
mvn compile exec:java -Dexec.mainClass="com.uadb.mentoruadb.config.DatabaseConnection"
```

## 3. Lancer l'application

```bash
mvn clean javafx:run
```

## 4. Structure du projet

```
src/main/java/.../Main.java              -> point d'entrée JavaFX
src/main/java/.../config/                -> DatabaseConnection (JDBC)
src/main/java/.../model/                 -> entités (11 classes, cf. MEA section 6)
src/main/java/.../service/               -> logique métier + accès aux données (JDBC)
src/main/java/.../controller/            -> contrôleurs FXML
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

---

Projet réalisé dans le cadre du cours de Java avancé — UADB.
