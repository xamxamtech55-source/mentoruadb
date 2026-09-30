-- =========================================================
-- MIGRATION 002 — partage individuel de fichier
-- Sûre à relancer plusieurs fois (idempotente) : n'ajoute que ce qui manque.
--
-- Usage :   mysql -u root -p < database/migration_002_partage_fichier_individuel.sql
--
-- Une base neuve n'en a pas besoin : schema.sql contient déjà tout cela.
-- =========================================================

USE mentoruadb;

DROP PROCEDURE IF EXISTS migration_002;

DELIMITER //

CREATE PROCEDURE migration_002()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fichier' AND COLUMN_NAME = 'id_etudiant') THEN

ALTER TABLE fichier ADD COLUMN id_etudiant INT NULL AFTER id_seance;

ALTER TABLE fichier
    ADD CONSTRAINT fk_fichier_etudiant FOREIGN KEY (id_etudiant)
        REFERENCES etudiant(id_etudiant) ON UPDATE CASCADE ON DELETE CASCADE;
END IF;
END //

DELIMITER ;

CALL migration_002();

DROP PROCEDURE migration_002;