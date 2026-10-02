-- =========================================================
-- MENTORUADB
-- SCHEMA DE LA BASE DE DONNÉES
-- MySQL 8.x
-- =========================================================

-- ---------------------------------------------------------
-- 1. CRÉATION DE LA BASE
-- ---------------------------------------------------------

CREATE DATABASE IF NOT EXISTS mentoruadb
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE mentoruadb;


-- =========================================================
-- 2. TABLE UTILISATEUR
-- =========================================================

CREATE TABLE utilisateur (
                             id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,

                             nom VARCHAR(100) NOT NULL,
                             prenom VARCHAR(100) NOT NULL,

                             email VARCHAR(150) NOT NULL UNIQUE,

                             mot_de_passe VARCHAR(255) NOT NULL,

                             role ENUM('ETUDIANT', 'MENTOR', 'ADMIN') NOT NULL,

                             statut ENUM('ACTIF', 'INACTIF') NOT NULL DEFAULT 'ACTIF',

    -- Champs de profil (sécurité/identification)
                             telephone VARCHAR(20) NULL,
                             photo VARCHAR(500) NULL
);


-- =========================================================
-- 3. TABLE UFR / INSTITUT
-- =========================================================

CREATE TABLE ufr (
                     id_ufr INT AUTO_INCREMENT PRIMARY KEY,
                     nom VARCHAR(150) NOT NULL UNIQUE
);


-- =========================================================
-- 4. TABLE FILIERE
-- =========================================================

CREATE TABLE filiere (
                         id_filiere INT AUTO_INCREMENT PRIMARY KEY,
                         nom VARCHAR(150) NOT NULL,
                         id_ufr INT NOT NULL,

                         CONSTRAINT uq_filiere_ufr UNIQUE (nom, id_ufr),

                         CONSTRAINT fk_filiere_ufr FOREIGN KEY (id_ufr)
                             REFERENCES ufr(id_ufr) ON UPDATE CASCADE ON DELETE RESTRICT
);


-- =========================================================
-- 5. TABLE NIVEAU
-- =========================================================

CREATE TABLE niveau (
                        id_niveau INT AUTO_INCREMENT PRIMARY KEY,
                        libelle VARCHAR(50) NOT NULL UNIQUE
);


-- =========================================================
-- 6. TABLE ASSOCIATION FILIERE <-> NIVEAU
-- =========================================================

CREATE TABLE filiere_niveau (
                                id_filiere INT NOT NULL,
                                id_niveau INT NOT NULL,

                                PRIMARY KEY (id_filiere, id_niveau),

                                CONSTRAINT fk_filiere_niveau_filiere FOREIGN KEY (id_filiere)
                                    REFERENCES filiere(id_filiere) ON UPDATE CASCADE ON DELETE CASCADE,

                                CONSTRAINT fk_filiere_niveau_niveau FOREIGN KEY (id_niveau)
                                    REFERENCES niveau(id_niveau) ON UPDATE CASCADE ON DELETE CASCADE
);


-- =========================================================
-- 7. TABLE ETUDIANT
-- =========================================================

CREATE TABLE etudiant (
                          id_etudiant INT AUTO_INCREMENT PRIMARY KEY,
                          id_utilisateur INT NOT NULL UNIQUE,
                          id_filiere INT NOT NULL,
                          id_niveau INT NOT NULL,

    -- Champ de profil (identification académique)
                          numero_carte VARCHAR(50) NULL,

                          CONSTRAINT fk_etudiant_utilisateur FOREIGN KEY (id_utilisateur)
                              REFERENCES utilisateur(id_utilisateur) ON UPDATE CASCADE ON DELETE CASCADE,

    /*
       Cette clé étrangère composite garantit que le niveau
       choisi est bien autorisé pour la filière choisie.
    */
                          CONSTRAINT fk_etudiant_filiere_niveau FOREIGN KEY (id_filiere, id_niveau)
                              REFERENCES filiere_niveau(id_filiere, id_niveau) ON UPDATE CASCADE ON DELETE RESTRICT
);


-- =========================================================
-- 8. TABLE MATIERE
-- =========================================================

CREATE TABLE matiere (
                         id_matiere INT AUTO_INCREMENT PRIMARY KEY,
                         nom VARCHAR(150) NOT NULL UNIQUE
);


-- =========================================================
-- 9. TABLE MENTOR
-- =========================================================

CREATE TABLE mentor (
                        id_mentor INT AUTO_INCREMENT PRIMARY KEY,
                        id_etudiant INT NOT NULL UNIQUE,

                        statut_validation ENUM('EN_ATTENTE', 'VALIDE', 'REFUSE') NOT NULL DEFAULT 'EN_ATTENTE',

    -- Champs de profil mentor
                        biographie TEXT NULL,
                        experience VARCHAR(255) NULL,
                        mode_preference ENUM('EN_LIGNE', 'PRESENTIEL', 'LES_DEUX') NULL,
                        nombre_max_mentores INT NULL DEFAULT 5,

                        CONSTRAINT fk_mentor_etudiant FOREIGN KEY (id_etudiant)
                            REFERENCES etudiant(id_etudiant) ON UPDATE CASCADE ON DELETE CASCADE
);


-- =========================================================
-- 10. TABLE EXPERTISE
-- =========================================================

CREATE TABLE expertise (
                           id_mentor INT NOT NULL,
                           id_matiere INT NOT NULL,

                           PRIMARY KEY (id_mentor, id_matiere),

                           CONSTRAINT fk_expertise_mentor FOREIGN KEY (id_mentor)
                               REFERENCES mentor(id_mentor) ON UPDATE CASCADE ON DELETE CASCADE,

                           CONSTRAINT fk_expertise_matiere FOREIGN KEY (id_matiere)
                               REFERENCES matiere(id_matiere) ON UPDATE CASCADE ON DELETE CASCADE
);


-- =========================================================
-- 11. TABLE DEMANDE DE MENTORAT
-- =========================================================

CREATE TABLE demande_mentorat (
                                  id_demande INT AUTO_INCREMENT PRIMARY KEY,
                                  id_etudiant INT NOT NULL,
                                  id_mentor INT NOT NULL,
                                  id_matiere INT NOT NULL,
                                  date_demande DATE NOT NULL,

                                  statut ENUM('EN_ATTENTE', 'ACCEPTEE', 'REFUSEE') NOT NULL DEFAULT 'EN_ATTENTE',

    -- Masquage d'une demande dans l'historique de l'étudiant / du mentor (sans la supprimer)
                                  masque_etudiant TINYINT(1) NOT NULL DEFAULT 0,
                                  masque_mentor TINYINT(1) NOT NULL DEFAULT 0,

                                  CONSTRAINT fk_demande_etudiant FOREIGN KEY (id_etudiant)
                                      REFERENCES etudiant(id_etudiant) ON UPDATE CASCADE ON DELETE RESTRICT,

                                  CONSTRAINT fk_demande_mentor FOREIGN KEY (id_mentor)
                                      REFERENCES mentor(id_mentor) ON UPDATE CASCADE ON DELETE RESTRICT,

                                  CONSTRAINT fk_demande_matiere FOREIGN KEY (id_matiere)
                                      REFERENCES matiere(id_matiere) ON UPDATE CASCADE ON DELETE RESTRICT
);


-- =========================================================
-- 12. TABLE SEANCE
-- Individuelle (liée à une demande) OU de groupe (liée à un
-- mentor + une matière, avec les participants dans seance_participant)
-- =========================================================

CREATE TABLE seance (
                        id_seance INT AUTO_INCREMENT PRIMARY KEY,

                        id_demande INT NULL,                                   -- rempli si INDIVIDUELLE
                        type_seance ENUM('INDIVIDUELLE', 'GROUPE') NOT NULL DEFAULT 'INDIVIDUELLE',
                        id_mentor INT NULL,                                     -- rempli si GROUPE
                        id_matiere INT NULL,                                    -- rempli si GROUPE

                        date_seance DATE NOT NULL,
                        heure_debut TIME NOT NULL,
                        heure_fin TIME NOT NULL,

                        statut ENUM('PLANIFIEE', 'REALISEE', 'ANNULEE') NOT NULL DEFAULT 'PLANIFIEE',

    -- Modalité de la séance
                        modalite ENUM('EN_LIGNE', 'PRESENTIEL') NOT NULL DEFAULT 'PRESENTIEL',
                        lieu VARCHAR(255) NULL,                                 -- salle ou lien visio

    -- Masquage d'une séance dans l'historique de l'étudiant / du mentor (sans la supprimer)
                        masque_etudiant TINYINT(1) NOT NULL DEFAULT 0,
                        masque_mentor TINYINT(1) NOT NULL DEFAULT 0,

                        CONSTRAINT fk_seance_demande FOREIGN KEY (id_demande)
                            REFERENCES demande_mentorat(id_demande) ON UPDATE CASCADE ON DELETE RESTRICT,

                        CONSTRAINT fk_seance_mentor FOREIGN KEY (id_mentor)
                            REFERENCES mentor(id_mentor) ON UPDATE CASCADE ON DELETE CASCADE,

                        CONSTRAINT fk_seance_matiere FOREIGN KEY (id_matiere)
                            REFERENCES matiere(id_matiere) ON UPDATE CASCADE ON DELETE CASCADE,

                        CONSTRAINT chk_heure_seance CHECK (heure_fin > heure_debut)
);


-- =========================================================
-- 13. TABLE PARTICIPANTS D'UNE SEANCE DE GROUPE
-- =========================================================

CREATE TABLE seance_participant (
                                    id_seance INT NOT NULL,
                                    id_etudiant INT NOT NULL,

    -- confirme : l'étudiant a confirmé sa présence ; vu : il a vu l'invitation ; masque : retirée de son historique
                                    confirme TINYINT(1) NOT NULL DEFAULT 0,
                                    vu TINYINT(1) NOT NULL DEFAULT 0,
                                    masque TINYINT(1) NOT NULL DEFAULT 0,

                                    PRIMARY KEY (id_seance, id_etudiant),

                                    CONSTRAINT fk_participant_seance FOREIGN KEY (id_seance)
                                        REFERENCES seance(id_seance) ON UPDATE CASCADE ON DELETE CASCADE,

                                    CONSTRAINT fk_participant_etudiant FOREIGN KEY (id_etudiant)
                                        REFERENCES etudiant(id_etudiant) ON UPDATE CASCADE ON DELETE CASCADE
);


-- =========================================================
-- 14. TABLE EVALUATION
-- =========================================================

CREATE TABLE evaluation (
                            id_evaluation INT AUTO_INCREMENT PRIMARY KEY,
                            id_seance INT NOT NULL,
                            id_etudiant INT NOT NULL,                              -- l'étudiant qui évalue

                            note TINYINT NOT NULL,
                            commentaire VARCHAR(500),
                            date_evaluation DATE NOT NULL,

                            masque_mentor TINYINT(1) NOT NULL DEFAULT 0,           -- le mentor masque l'évaluation de sa vue

                            CONSTRAINT chk_note CHECK (note BETWEEN 1 AND 5),

    -- Une évaluation par étudiant et par séance (indispensable pour les séances de groupe)
                            CONSTRAINT uq_evaluation_seance_etudiant UNIQUE (id_seance, id_etudiant),

                            CONSTRAINT fk_evaluation_seance FOREIGN KEY (id_seance)
                                REFERENCES seance(id_seance) ON UPDATE CASCADE ON DELETE CASCADE,

                            CONSTRAINT fk_evaluation_etudiant FOREIGN KEY (id_etudiant)
                                REFERENCES etudiant(id_etudiant) ON UPDATE CASCADE ON DELETE CASCADE
);


-- =========================================================
-- 14 bis. TABLE FILIERE_MATIERE (matières enseignées dans une filière)
-- Utilisée pour suggérer des mentors et lister les matières d'une filière
-- =========================================================

CREATE TABLE filiere_matiere (
                                 id_filiere INT NOT NULL,
                                 id_matiere INT NOT NULL,

                                 PRIMARY KEY (id_filiere, id_matiere),

                                 CONSTRAINT fk_filiere_matiere_filiere FOREIGN KEY (id_filiere)
                                     REFERENCES filiere(id_filiere) ON UPDATE CASCADE ON DELETE CASCADE,

                                 CONSTRAINT fk_filiere_matiere_matiere FOREIGN KEY (id_matiere)
                                     REFERENCES matiere(id_matiere) ON UPDATE CASCADE ON DELETE CASCADE
);


-- =========================================================
-- 12 bis. TABLE DEMANDE_SEANCE
-- Une fois une demande_mentorat ACCEPTEE, l'étudiant peut demander une séance quand il en a
-- besoin. Le mentor l'accepte (il choisit alors date/heure/modalité/lieu selon sa disponibilité,
-- ce qui crée la séance) ou la refuse.
-- =========================================================

CREATE TABLE demande_seance (
                                id_demande_seance INT AUTO_INCREMENT PRIMARY KEY,
                                id_demande INT NOT NULL,                                 -- la demande_mentorat (doit être ACCEPTEE)

                                date_souhaitee DATE NULL,                                -- suggestion optionnelle de l'étudiant
                                message VARCHAR(500) NULL,                               -- motif, disponibilités, etc.

                                statut ENUM('EN_ATTENTE', 'ACCEPTEE', 'REFUSEE') NOT NULL DEFAULT 'EN_ATTENTE',
                                date_creation DATE NOT NULL,

                                id_seance INT NULL,                                      -- rempli quand ACCEPTEE : la séance créée

                                masque_etudiant TINYINT(1) NOT NULL DEFAULT 0,
                                masque_mentor TINYINT(1) NOT NULL DEFAULT 0,

                                CONSTRAINT fk_demandeseance_demande FOREIGN KEY (id_demande)
                                    REFERENCES demande_mentorat(id_demande) ON UPDATE CASCADE ON DELETE CASCADE,

                                CONSTRAINT fk_demandeseance_seance FOREIGN KEY (id_seance)
                                    REFERENCES seance(id_seance) ON UPDATE CASCADE ON DELETE SET NULL
);


-- =========================================================
-- 12 ter. TABLE NOTIFICATION
-- Notification générique adressée à un compte utilisateur (étudiant ou mentor).
-- =========================================================

CREATE TABLE notification (
                              id_notification INT AUTO_INCREMENT PRIMARY KEY,
                              id_utilisateur INT NOT NULL,

                              message VARCHAR(500) NOT NULL,
                              lue TINYINT(1) NOT NULL DEFAULT 0,
                              date_creation DATETIME NOT NULL,

                              CONSTRAINT fk_notification_utilisateur FOREIGN KEY (id_utilisateur)
                                  REFERENCES utilisateur(id_utilisateur) ON UPDATE CASCADE ON DELETE CASCADE
);


-- =========================================================
-- 15. TABLE FICHIER (ressources partagées par un mentor)
-- =========================================================

CREATE TABLE fichier (
                         id_fichier INT AUTO_INCREMENT PRIMARY KEY,
                         id_mentor INT NOT NULL,
                         id_matiere INT NULL,
                         id_seance INT NULL,

    -- id_etudiant NULL      : fichier partagé avec TOUS les étudiants ayant une demande acceptée
    --                         pour ce mentor + cette matière (comportement historique).
    -- id_etudiant renseigné : fichier partagé avec CET étudiant précis uniquement (par exemple un
    --                         étudiant arrivé en retard, à qui on renvoie une ressource déjà
    --                         partagée aux autres, sans redéranger ceux qui l'ont déjà reçue).
                         id_etudiant INT NULL,

                         nom_fichier VARCHAR(255) NOT NULL,
                         chemin VARCHAR(500) NOT NULL,
                         date_upload DATE NOT NULL,

                         CONSTRAINT fk_fichier_mentor FOREIGN KEY (id_mentor)
                             REFERENCES mentor(id_mentor) ON UPDATE CASCADE ON DELETE CASCADE,

                         CONSTRAINT fk_fichier_matiere FOREIGN KEY (id_matiere)
                             REFERENCES matiere(id_matiere) ON UPDATE CASCADE ON DELETE CASCADE,

                         CONSTRAINT fk_fichier_seance FOREIGN KEY (id_seance)
                             REFERENCES seance(id_seance) ON UPDATE CASCADE ON DELETE CASCADE,

                         CONSTRAINT fk_fichier_etudiant FOREIGN KEY (id_etudiant)
                             REFERENCES etudiant(id_etudiant) ON UPDATE CASCADE ON DELETE CASCADE
);


-- =========================================================
-- FIN DU SCHEMA (15 entités)
-- =========================================================