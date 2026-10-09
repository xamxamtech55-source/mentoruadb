# CLAUDE.md — MentorUADB

Fichier de mémoire du projet. Claude Code le lit au début de chaque session : on le met à jour à la fin de chaque session (section « Journal »).

## Projet

MentorUADB : application desktop de mentorat étudiant pour l'Université Alioune Diop de Bambey (UADB). Projet universitaire (cours de Java avancé), mené seul par Aly. Dépôt : xamxamtech55-source/mentoruadb, branche `main`.

Stack : Java 21, JavaFX 21.0.2 (FXML + CSS), JDBC, MySQL 8 (Connector/J 8.3.0), JUnit 5.10.2, Maven 3.9+.

## Architecture (à respecter)

JavaFX/FXML/CSS → Controllers → Services → DAO → JDBC → MySQL

- Aucun SQL dans les contrôleurs. Requêtes paramétrées (PreparedStatement) uniquement dans les DAO.
- Règles métier et transactions dans les services (InscriptionService, MentoratService).
- Un DAO par entité, un contrôleur par scène FXML.
- Fermeture des ressources JDBC avec try-with-resources, pas de e.printStackTrace dans le code livré.
- Configuration BDD externalisée (db.properties ignoré par Git ou variables d'environnement).
- Mots de passe : PBKDF2-HMAC-SHA256 avec sel.

## Domaine

Rôles : ETUDIANT, MENTOR, ADMIN (statut ACTIF/INACTIF). Un mentor reste un étudiant : ETUDIANT et MENTOR vont dans l'espace étudiant, seul ADMIN a l'espace admin.

Flux : demande de mentorat (EN_ATTENTE → ACCEPTEE/REFUSEE) puis demande de séance (seulement après mentorat accepté) → séance (PLANIFIEE/REALISEE/ANNULEE), individuelle ou de groupe → évaluation 1 à 5.

Règles clés : email @uadb.edu.sn (RG19) ; Licence 1 ne peut pas être mentor (RG20) ; infos mentor (biographie, expérience, mode) seulement à la candidature (RG21) ; capacité max informative (RG22) ; matière demandée ∈ expertises du mentor (RG23) ; heure_fin > heure_debut (RG11) ; une évaluation par étudiant et séance (RG15) ; historiques masqués (colonnes masque_*), jamais supprimés (RG17, RG26) ; notification à chaque changement de statut (RG27).

Schéma : 17 tables (utilisateur, ufr, filiere, niveau, filiere_niveau, etudiant, matiere, filiere_matiere, mentor, expertise, demande_mentorat, demande_seance, seance, seance_participant, evaluation, notification, fichier). `database/schema.sql` fait foi.

Cahier des charges V1.1 (avec conception) : doc partagé dans Claude ; à finaliser après les changements d'écrans.

## Chantier en cours : refonte des écrans étudiant/mentor

Constat sur le dépôt (08/10/2026) : l'admin n'a pas de menu latéral ; `dashboard-admin.fxml` est un menu de boutons qui ouvre des écrans dédiés, dont `gestion-donnees.fxml` (TabPane + TreeView UFR → filières → matières). Les dashboards étudiant et mentor étaient de longues pages à défiler avec 6 à 8 tableaux empilés.

Décision : reprendre le modèle à onglets de `gestion-donnees.fxml` (TabPane dans le dashboard, même en-tête, notifications au-dessus des onglets). Les `fx:id` et les `onAction` ne changent pas : les contrôleurs restent inchangés.

Dashboard étudiant : onglets « Mes demandes » (recherche de mentor, demandes de mentorat, demandes de séance), « Mes séances » (à confirmer, séances, évaluation), « Ressources ».
Dashboard mentor : onglets « Demandes reçues » (mentorat + séances, planification), « Mes séances » (individuelles + groupe), « Évaluations », « Fichiers ».

Fait : nouveaux `dashboard-etudiant.fxml` et `dashboard-mentor.fxml` fournis (à tester).

Reste à faire (une branche `feature/...` par étape, test manuel avant fusion) :
1. Tester les deux dashboards (compilation, JavaFX, enchaînement des actions).
2. Écran de recherche de mentor : arbre UFR → filière → matière comme filtre (même principe que l'admin) ; actuellement `recherche-mentor.fxml` est une page à défiler avec un ComboBox de matières.
3. Faire basculer l'onglet actif depuis les notifications (optionnel).
4. Faire respecter RG20 (Licence 1 non mentor) côté service : aujourd'hui seul le bouton est masqué dans DashboardEtudiantController.
5. Faire respecter RG22 si souhaité (capacité max non appliquée).
6. Mettre à jour la section 9.8 du cahier des charges. — **fait (branche `feature/docs-organisation`)** : README et CAHIER_DE_CHARGE V1.1 à jour (écrans, limites).

## Conventions UI

- Fenêtre de taille fixe 950 × 700, logo UADB, titre « Mentor UADB ».
- Garder le style CSS de l'admin (couleurs, boutons, tableaux) ; couleurs de statut homogènes.
- Confirmer les actions sensibles (refus, annulation, masquage).
- Navigation par une classe utilitaire unique de changement de scène.

## Façon de travailler

- Langue : français (code et noms de classes en cohérence avec l'existant).
- Avant de modifier un écran, lire son FXML, son CSS et son contrôleur ; ne pas deviner les noms existants.
- Petites étapes, une fonctionnalité à la fois ; le code doit compiler à chaque étape.
- Ne jamais pousser sur `main` directement : branche, test, Pull Request, fusion.

## Journal des sessions

À compléter à la fin de chaque session : date, ce qui est fait, ce qui reste, décisions prises.

- 2026-10-08 : cahier des charges V1.1 rédigé (règles RG19-RG28, recette T01-T30, conception). Dépôt relu : écran admin = menu + écrans à onglets. Nouveaux dashboards étudiant et mentor à onglets fournis (fx:id et actions inchangés). Prochaine étape : tester, puis écran de recherche avec arbre UFR → filière → matière.
- 2026-10-08 (suite) : branche `feature/dashboard-etudiant-organisation` — dashboard étudiant aligné sur le mentor (4 onglets, TabPane 540) avec un nouvel onglet « Évaluations » listant les évaluations données (DTO `EvaluationEtudiantVue`, `EvaluationDao.findByEtudiantAvecDetails`). Branche locale non poussée.
- 2026-10-08 (suite) : branche `feature/reinitialisation-email` — réinitialisation de mot de passe par mail en 2 écrans (email + n° de carte → code à 6 chiffres envoyé par mail → code + nouveau mot de passe). Nouveaux `ReinitialisationService`, `ReinitialisationDao`, `MailUtil` (SMTP Gmail, mot de passe d'application), table `reinitialisation_mot_de_passe` + `migration_004`. Code haché (PBKDF2), expiration 15 min, 5 tentatives max, même message que l'identité soit bonne ou non (pas d'énumération d'email). Config SMTP externalisée : `mail.properties` (ignoré par Git) ou variables `MENTORUADB_MAIL_*`, exemple `mail.properties.example`. Testé de bout en bout (faux serveur SMTP local + `mvn test`). Reste : configurer un vrai compte Gmail, test manuel des 2 écrans, pousser les 2 branches et ouvrir les PR.
- 2026-10-08 (suite) : le code de réinitialisation n'est plus envoyé à l'email institutionnel mais à l'**email de récupération** du compte (Gmail ou autre), renseigné dans « Mon profil ». Nouvelle colonne `utilisateur.email_recuperation` (`migration_005`), champ dans `profil.fxml`/`ProfilController`. `ReinitialisationService.envoyerCode` retourne l'adresse d'envoi (`Optional<String>`) ; si aucun email de récupération n'est renseigné, on refuse silencieusement (même message). Testé de bout en bout avec le faux serveur SMTP (mail reçu sur le Gmail, code → mdp changé).
- 2026-10-08 (suite) : à l'écran « mot de passe oublié », l'adresse qui reçoit le code est désormais un champ saisissable (n'importe quel Gmail ou autre), pré-rempli avec l'email de récupération du compte quand il existe, mais modifiable. `ReinitialisationService.envoyerCode(email, carte, emailRecuperation)` : si le champ est vide, repli sur `utilisateur.email_recuperation` ; sinon envoi à l'adresse tapée (format validé). Testé avec le faux SMTP : champ vide → email du compte, adresse tapée → cette adresse, carte fausse ou compte sans adresse → refus silencieux, aucun mail.
- 2026-10-08 (suite) : l'email de récupération (Gmail…) peut aussi être saisi **à l'inscription** (`inscription.fxml` + `InscriptionController`), champ facultatif reçoit le code de réinitialisation. `InscriptionService.inscrireEtudiant`/`inscrireMentor` prennent un paramètre `emailRecuperation` (validé si non vide, sinon null). Testé : inscription avec/sans Gmail, format invalide refusé.
- 2026-10-09 : écran d'accueil `bienvenue.fxml` remanié façon héros d'application web : photo « étudiants à la bibliothèque » (`images/etudiants-bibliotheque.jpg`, Pexels, libre de droits) en fond plein cadre avec voile dégradé pour la lisibilité (`.bienvenue-voile`), effet Ken Burns (zoom + panoramique lents, en boucle) et apparition échelonnée des cartes dans `BienvenueController`. Survol des cartes remonté (`-fx-translate-y`). Reste : test visuel par Aly sur son écran.
- 2026-10-09 (suite) : à l'inscription, le texte « facultatif » du champ email de récupération est intégré au `promptText` (le label séparé prenait trop de place). Branches `feature/dashboard-etudiant-organisation` et `feature/reinitialisation-email` poussées sur `origin` ; PR #1 et #2 ouvertes vers `main`.
- 2026-10-09 (suite) : branche `feature/docs-organisation` — `README.md` réécrit (16 écrans, fonctionnalités, config SMTP, migrations 001-005, sécurité/limites à jour) et `CAHIER_DE_CHARGE.md` passé en **V1.1** (écrans 10.1-10.5 à jour : accueil animé, dashboards à onglets, réinitialisation en 2 écrans + email de récupération ; §5.5 et recette à 16 écrans ; §12 sécurité + SMTP ; §13 limites L-01 à L-07). Nettoyage racine : dossier vide `DISPLAY/` supprimé et ajouté à `.gitignore`. L'équipe affichée dans le README reste volontairement le tableau à 4 membres.
