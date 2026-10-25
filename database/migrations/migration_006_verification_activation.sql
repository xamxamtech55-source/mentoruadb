-- ============================================================
-- MIGRATION 006 — vérification universitaire et première activation
--
-- Ajoute :
--   1. le référentiel universitaire de confiance (données fictives au départ) ;
--   2. les demandes d'activation de compte (code à usage unique + ticket de
--      définition du mot de passe, tous deux stockés hachés) ;
--   3. le lien entre un compte étudiant et son dossier du référentiel ;
--   4. l'indicateur persistant `must_change_password` ;
--   5. les contraintes d'unicité anti-doublon (carte, dossier, email).
--
-- Sûre à relancer plusieurs fois (idempotente).
--
-- Usage :   mysql -u root -p < database/migrations/migration_006_verification_activation.sql
--
-- Une base neuve n'en a pas besoin : database/schema/schema.sql contient déjà tout cela.
-- ============================================================

USE mentoruadb;

-- Les transactions et les clés étrangères exigent InnoDB : les tables
-- historiques de la base (créées en MyISAM) sont converties avant les
-- contraintes de cette migration.
ALTER TABLE utilisateur ENGINE = InnoDB;
ALTER TABLE etudiant ENGINE = InnoDB;
ALTER TABLE filiere ENGINE = InnoDB;
ALTER TABLE niveau ENGINE = InnoDB;
ALTER TABLE filiere_niveau ENGINE = InnoDB;

-- ------------------------------------------------------------
-- 1. Référentiel universitaire (source de vérité académique).
--    Remplacé plus tard par un connecteur vers le système officiel
--    autorisé : c'est la seule couche à adapter.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS referentiel_etudiant (
    id_referentiel INT AUTO_INCREMENT PRIMARY KEY,

    numero_carte VARCHAR(20) NOT NULL,
    email_institutionnel VARCHAR(150) NOT NULL,
    -- Adresse personnelle enregistrée : destinataire du code et preuve saisie à l'étape 1.
    email_personnel VARCHAR(150) NOT NULL,

    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,

    statut_inscription ENUM('ACTIF', 'INACTIF') NOT NULL DEFAULT 'ACTIF',

    id_filiere INT NOT NULL,
    id_niveau INT NOT NULL,
    annee_academique VARCHAR(20) NOT NULL,

    date_mise_a_jour DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_referentiel_carte UNIQUE (numero_carte),
    CONSTRAINT uq_referentiel_email UNIQUE (email_institutionnel),

    CONSTRAINT fk_referentiel_filiere FOREIGN KEY (id_filiere)
        REFERENCES filiere(id_filiere) ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT fk_referentiel_niveau FOREIGN KEY (id_niveau)
        REFERENCES niveau(id_niveau) ON UPDATE CASCADE ON DELETE RESTRICT,

    -- Le couple filière/niveau doit exister dans filiere_niveau (même règle que etudiant).
    CONSTRAINT fk_referentiel_filiere_niveau FOREIGN KEY (id_filiere, id_niveau)
        REFERENCES filiere_niveau(id_filiere, id_niveau) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- Reprise si une première version de la table existait sans l'email personnel.
SET @existe := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'referentiel_etudiant'
      AND COLUMN_NAME = 'email_personnel'
);
SET @sql := IF(@existe = 0,
    'ALTER TABLE referentiel_etudiant ADD COLUMN email_personnel VARCHAR(150) NOT NULL DEFAULT '''' AFTER email_institutionnel',
    'SELECT "colonne email_personnel déjà présente" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 2. Colonne utilisateur.must_change_password
-- ------------------------------------------------------------
SET @existe := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'utilisateur'
      AND COLUMN_NAME = 'must_change_password'
);
SET @sql := IF(@existe = 0,
    'ALTER TABLE utilisateur ADD COLUMN must_change_password TINYINT(1) NOT NULL DEFAULT 0 AFTER email_recuperation',
    'SELECT "colonne must_change_password déjà présente" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 3. Lien etudiant -> référentiel
-- ------------------------------------------------------------
SET @existe := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'etudiant'
      AND COLUMN_NAME = 'id_referentiel'
);
SET @sql := IF(@existe = 0,
    'ALTER TABLE etudiant ADD COLUMN id_referentiel INT NULL AFTER numero_carte',
    'SELECT "colonne id_referentiel déjà présente" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @existe := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'etudiant'
      AND INDEX_NAME = 'uq_etudiant_referentiel'
);
SET @sql := IF(@existe = 0,
    'ALTER TABLE etudiant ADD CONSTRAINT uq_etudiant_referentiel UNIQUE (id_referentiel)',
    'SELECT "index uq_etudiant_referentiel déjà présent" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @existe := (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'etudiant'
      AND CONSTRAINT_NAME = 'fk_etudiant_referentiel'
);
SET @sql := IF(@existe = 0,
    'ALTER TABLE etudiant ADD CONSTRAINT fk_etudiant_referentiel FOREIGN KEY (id_referentiel) REFERENCES referentiel_etudiant(id_referentiel) ON UPDATE CASCADE ON DELETE SET NULL',
    'SELECT "contrainte fk_etudiant_referentiel déjà présente" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Un numéro de carte identifie un seul profil étudiant (NULL reste autorisé
-- plusieurs fois : les anciens comptes sans carte ne sont pas bloqués).
SET @existe := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'etudiant'
      AND INDEX_NAME = 'uq_etudiant_numero_carte'
);
-- Des doublons issus d'anciennes données de test empêchent la contrainte :
-- on ne bloque pas la migration, mais on signale la vérification à faire.
SET @doublons := (
    SELECT COUNT(*) FROM (
        SELECT numero_carte FROM etudiant
        WHERE numero_carte IS NOT NULL
        GROUP BY numero_carte HAVING COUNT(*) > 1
    ) d
);
SET @sql := IF(@existe > 0,
    'SELECT "index uq_etudiant_numero_carte déjà présent" AS info',
    IF(@doublons = 0,
        'ALTER TABLE etudiant ADD CONSTRAINT uq_etudiant_numero_carte UNIQUE (numero_carte)',
        'SELECT "doublons de numero_carte : contrainte d''unicité NON ajoutée, à corriger" AS info'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 4. Demandes d'activation (code + ticket de mot de passe)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS activation_compte (
    id_activation INT AUTO_INCREMENT PRIMARY KEY,

    id_referentiel INT NOT NULL,
    -- Rempli quand le compte applicatif est créé (définition du mot de passe).
    id_utilisateur INT NULL,

    -- Le code à 6 chiffres n'est jamais stocké en clair : hachage PBKDF2.
    code_hash VARCHAR(255) NOT NULL,
    -- 1 = code déjà validé : il ne peut plus resservir, seul le ticket agit.
    code_utilise TINYINT(1) NOT NULL DEFAULT 0,
    -- États explicites du parcours de première connexion.
    etat ENUM('CODE_EN_ATTENTE', 'CODE_VALIDE', 'MOT_DE_PASSE_A_DEFINIR',
              'UTILISE', 'ANNULE') NOT NULL DEFAULT 'CODE_EN_ATTENTE',
    -- Jeton remis à l'écran « choix du mot de passe » (stocké haché, usage unique).
    ticket_hash VARCHAR(255) NULL,

    -- Adresse de réception du code (Gmail ou autre) : jamais utilisée comme
    -- preuve d'identité universitaire, seulement comme canal d'envoi.
    destinataire VARCHAR(150) NOT NULL,

    date_expiration DATETIME NOT NULL,
    tentatives TINYINT NOT NULL DEFAULT 0,
    utilise TINYINT(1) NOT NULL DEFAULT 0,
    date_creation DATETIME NOT NULL,

    CONSTRAINT fk_activation_referentiel FOREIGN KEY (id_referentiel)
        REFERENCES referentiel_etudiant(id_referentiel) ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT fk_activation_utilisateur FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur) ON UPDATE CASCADE ON DELETE CASCADE
);

-- Colonne ajoutée ensuite : reprise possible si une première version de la
-- table existait déjà sans elle.
SET @existe := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'activation_compte'
      AND COLUMN_NAME = 'code_utilise'
);
SET @sql := IF(@existe = 0,
    'ALTER TABLE activation_compte ADD COLUMN code_utilise TINYINT(1) NOT NULL DEFAULT 0 AFTER code_hash',
    'SELECT "colonne code_utilise dǸj�� prǸsente" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @existe := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'activation_compte'
      AND COLUMN_NAME = 'etat'
);
SET @sql := IF(@existe = 0,
    'ALTER TABLE activation_compte ADD COLUMN etat ENUM(''CODE_EN_ATTENTE'', ''CODE_VALIDE'', ''MOT_DE_PASSE_A_DEFINIR'', ''UTILISE'', ''ANNULE'') NOT NULL DEFAULT ''CODE_EN_ATTENTE'' AFTER code_utilise',
    'SELECT "colonne etat dǸj�� prǸsente" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @existe := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'activation_compte'
      AND INDEX_NAME = 'idx_activation_referentiel'
);
SET @sql := IF(@existe = 0,
    'CREATE INDEX idx_activation_referentiel ON activation_compte (id_referentiel, utilise)',
    'SELECT "index idx_activation_referentiel déjà présent" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @existe := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'activation_compte'
      AND INDEX_NAME = 'idx_activation_utilisateur'
);
SET @sql := IF(@existe = 0,
    'CREATE INDEX idx_activation_utilisateur ON activation_compte (id_utilisateur, utilise)',
    'SELECT "index idx_activation_utilisateur déjà présent" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @existe := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'activation_compte'
      AND INDEX_NAME = 'idx_activation_ticket'
);
SET @sql := IF(@existe = 0,
    'CREATE INDEX idx_activation_ticket ON activation_compte (ticket_hash)',
    'SELECT "index idx_activation_ticket déjà présent" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
