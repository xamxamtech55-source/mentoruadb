-- =========================================================
-- MIGRATION 001 — aligner une base EXISTANTE sur le code
-- Sûre à relancer plusieurs fois (idempotente) : n'ajoute que ce qui manque.
--
-- Usage :   mysql -u root -p < database/migration_001_alignement_code.sql
-- À faire : sauvegarder la base avant (mysqldump -u root -p mentoruadb > sauvegarde.sql)
--
-- Une base neuve n'en a pas besoin : schema.sql contient déjà tout cela.
-- =========================================================

USE mentoruadb;

DROP PROCEDURE IF EXISTS ajouter_colonne_si_absente;
DROP PROCEDURE IF EXISTS migration_001;

DELIMITER //

CREATE PROCEDURE ajouter_colonne_si_absente(
    IN p_table VARCHAR(64), IN p_colonne VARCHAR(64), IN p_definition VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_colonne) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_colonne, '` ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

CREATE PROCEDURE migration_001()
BEGIN
    -- 1. Colonnes de masquage / suivi -----------------------------------------
    CALL ajouter_colonne_si_absente('demande_mentorat', 'masque_etudiant', 'TINYINT(1) NOT NULL DEFAULT 0');
    CALL ajouter_colonne_si_absente('demande_mentorat', 'masque_mentor',   'TINYINT(1) NOT NULL DEFAULT 0');
    CALL ajouter_colonne_si_absente('seance',           'masque_etudiant', 'TINYINT(1) NOT NULL DEFAULT 0');
    CALL ajouter_colonne_si_absente('seance',           'masque_mentor',   'TINYINT(1) NOT NULL DEFAULT 0');
    CALL ajouter_colonne_si_absente('seance_participant', 'confirme', 'TINYINT(1) NOT NULL DEFAULT 0');
    CALL ajouter_colonne_si_absente('seance_participant', 'vu',       'TINYINT(1) NOT NULL DEFAULT 0');
    CALL ajouter_colonne_si_absente('seance_participant', 'masque',   'TINYINT(1) NOT NULL DEFAULT 0');
    CALL ajouter_colonne_si_absente('evaluation', 'masque_mentor', 'TINYINT(1) NOT NULL DEFAULT 0');

    -- 2. evaluation.id_etudiant (ajouté NULLABLE, rempli, puis passé NOT NULL) --
    CALL ajouter_colonne_si_absente('evaluation', 'id_etudiant', 'INT NULL');

    -- Séance individuelle : l'évaluateur est l'étudiant de la demande.
    -- Séance de groupe : évaluateur inconnu pour les anciennes lignes -> on prend le premier participant.
    UPDATE evaluation ev
        JOIN seance s ON s.id_seance = ev.id_seance
        LEFT JOIN demande_mentorat d ON d.id_demande = s.id_demande
    SET ev.id_etudiant = COALESCE(
            d.id_etudiant,
            (SELECT MIN(sp.id_etudiant) FROM seance_participant sp WHERE sp.id_seance = s.id_seance))
    WHERE ev.id_etudiant IS NULL;

    -- Échoue volontairement s'il reste des évaluations sans étudiant : à corriger à la main.
    ALTER TABLE evaluation MODIFY id_etudiant INT NOT NULL;

    IF NOT EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'evaluation'
                     AND CONSTRAINT_NAME = 'fk_evaluation_etudiant') THEN
        ALTER TABLE evaluation
            ADD CONSTRAINT fk_evaluation_etudiant FOREIGN KEY (id_etudiant)
                REFERENCES etudiant(id_etudiant) ON UPDATE CASCADE ON DELETE CASCADE;
    END IF;

    -- 3. Unicité : une évaluation par (séance, étudiant) au lieu d'une par séance --
    -- On ajoute d'abord le nouvel index (il sert aussi de support à la clé étrangère sur id_seance),
    -- puis on supprime l'ancien index unique sur id_seance seul.
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'evaluation'
                     AND INDEX_NAME = 'uq_evaluation_seance_etudiant') THEN
        ALTER TABLE evaluation
            ADD CONSTRAINT uq_evaluation_seance_etudiant UNIQUE (id_seance, id_etudiant);
    END IF;

    SET @ancien_index = NULL;
    SELECT INDEX_NAME INTO @ancien_index
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'evaluation'
      AND NON_UNIQUE = 0 AND INDEX_NAME <> 'PRIMARY'
    GROUP BY INDEX_NAME
    HAVING COUNT(*) = 1 AND MAX(COLUMN_NAME) = 'id_seance'
    LIMIT 1;

    IF @ancien_index IS NOT NULL THEN
        SET @ddl = CONCAT('ALTER TABLE evaluation DROP INDEX `', @ancien_index, '`');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

-- 4. Table filiere_matiere (utilisée par FiliereDao, FiliereMatiereDao, MentorDao) ---
CREATE TABLE IF NOT EXISTS filiere_matiere (
    id_filiere INT NOT NULL,
    id_matiere INT NOT NULL,
    PRIMARY KEY (id_filiere, id_matiere),
    CONSTRAINT fk_filiere_matiere_filiere FOREIGN KEY (id_filiere)
        REFERENCES filiere(id_filiere) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_filiere_matiere_matiere FOREIGN KEY (id_matiere)
        REFERENCES matiere(id_matiere) ON UPDATE CASCADE ON DELETE CASCADE
);

CALL migration_001();

DROP PROCEDURE migration_001;
DROP PROCEDURE ajouter_colonne_si_absente;
