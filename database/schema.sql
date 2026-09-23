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

                             role ENUM(
        'ETUDIANT',
        'MENTOR',
        'ADMIN'
    ) NOT NULL,

                             statut ENUM(
        'ACTIF',
        'INACTIF'
    ) NOT NULL DEFAULT 'ACTIF'
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

                         CONSTRAINT uq_filiere_ufr
                             UNIQUE (nom, id_ufr),

                         CONSTRAINT fk_filiere_ufr
                             FOREIGN KEY (id_ufr)
                                 REFERENCES ufr(id_ufr)
                                 ON UPDATE CASCADE
                                 ON DELETE RESTRICT
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

                                CONSTRAINT fk_filiere_niveau_filiere
                                    FOREIGN KEY (id_filiere)
                                        REFERENCES filiere(id_filiere)
                                        ON UPDATE CASCADE
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_filiere_niveau_niveau
                                    FOREIGN KEY (id_niveau)
                                        REFERENCES niveau(id_niveau)
                                        ON UPDATE CASCADE
                                        ON DELETE CASCADE
);


-- =========================================================
-- 7. TABLE ETUDIANT
-- =========================================================

CREATE TABLE etudiant (
                          id_etudiant INT AUTO_INCREMENT PRIMARY KEY,

                          id_utilisateur INT NOT NULL UNIQUE,

                          id_filiere INT NOT NULL,

                          id_niveau INT NOT NULL,

                          CONSTRAINT fk_etudiant_utilisateur
                              FOREIGN KEY (id_utilisateur)
                                  REFERENCES utilisateur(id_utilisateur)
                                  ON UPDATE CASCADE
                                  ON DELETE CASCADE,

    /*
       Cette clé étrangère composite garantit que le niveau
       choisi est bien autorisé pour la filière choisie.
    */
                          CONSTRAINT fk_etudiant_filiere_niveau
                              FOREIGN KEY (id_filiere, id_niveau)
                                  REFERENCES filiere_niveau(id_filiere, id_niveau)
                                  ON UPDATE CASCADE
                                  ON DELETE RESTRICT
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

                        statut_validation ENUM(
        'EN_ATTENTE',
        'VALIDE',
        'REFUSE'
    ) NOT NULL DEFAULT 'EN_ATTENTE',

                        CONSTRAINT fk_mentor_etudiant
                            FOREIGN KEY (id_etudiant)
                                REFERENCES etudiant(id_etudiant)
                                ON UPDATE CASCADE
                                ON DELETE CASCADE
);


-- =========================================================
-- 10. TABLE EXPERTISE
-- =========================================================

CREATE TABLE expertise (
                           id_mentor INT NOT NULL,

                           id_matiere INT NOT NULL,

                           PRIMARY KEY (id_mentor, id_matiere),

                           CONSTRAINT fk_expertise_mentor
                               FOREIGN KEY (id_mentor)
                                   REFERENCES mentor(id_mentor)
                                   ON UPDATE CASCADE
                                   ON DELETE CASCADE,

                           CONSTRAINT fk_expertise_matiere
                               FOREIGN KEY (id_matiere)
                                   REFERENCES matiere(id_matiere)
                                   ON UPDATE CASCADE
                                   ON DELETE CASCADE
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

                                  statut ENUM(
        'EN_ATTENTE',
        'ACCEPTEE',
        'REFUSEE'
    ) NOT NULL DEFAULT 'EN_ATTENTE',

                                  CONSTRAINT fk_demande_etudiant
                                      FOREIGN KEY (id_etudiant)
                                          REFERENCES etudiant(id_etudiant)
                                          ON UPDATE CASCADE
                                          ON DELETE RESTRICT,

                                  CONSTRAINT fk_demande_mentor
                                      FOREIGN KEY (id_mentor)
                                          REFERENCES mentor(id_mentor)
                                          ON UPDATE CASCADE
                                          ON DELETE RESTRICT,

                                  CONSTRAINT fk_demande_matiere
                                      FOREIGN KEY (id_matiere)
                                          REFERENCES matiere(id_matiere)
                                          ON UPDATE CASCADE
                                          ON DELETE RESTRICT
);


-- =========================================================
-- 12. TABLE SEANCE
-- =========================================================

CREATE TABLE seance (
                        id_seance INT AUTO_INCREMENT PRIMARY KEY,

                        id_demande INT NOT NULL,

                        date_seance DATE NOT NULL,

                        heure_debut TIME NOT NULL,

                        heure_fin TIME NOT NULL,

                        statut ENUM(
        'PLANIFIEE',
        'REALISEE',
        'ANNULEE'
    ) NOT NULL DEFAULT 'PLANIFIEE',

                        CONSTRAINT fk_seance_demande
                            FOREIGN KEY (id_demande)
                                REFERENCES demande_mentorat(id_demande)
                                ON UPDATE CASCADE
                                ON DELETE RESTRICT,

                        CONSTRAINT chk_heure_seance
                            CHECK (heure_fin > heure_debut)
);


-- =========================================================
-- 13. TABLE EVALUATION
-- =========================================================

CREATE TABLE evaluation (
                            id_evaluation INT AUTO_INCREMENT PRIMARY KEY,

                            id_seance INT NOT NULL UNIQUE,

                            note TINYINT NOT NULL,

                            commentaire VARCHAR(500),

                            date_evaluation DATE NOT NULL,

                            CONSTRAINT chk_note
                                CHECK (note BETWEEN 1 AND 5),

                            CONSTRAINT fk_evaluation_seance
                                FOREIGN KEY (id_seance)
                                    REFERENCES seance(id_seance)
                                    ON UPDATE CASCADE
                                    ON DELETE CASCADE
);


-- =========================================================
-- FIN DU SCHEMA
-- =========================================================