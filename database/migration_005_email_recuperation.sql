-- ============================================================
-- MIGRATION 005 — email de récupération (reçoit le code de réinitialisation)
-- Sûre à relancer plusieurs fois (idempotente) : n'ajoute que ce qui manque.
--
-- Usage :   mysql -u root -p < database/migration_005_email_recuperation.sql
--
-- Une base neuve n'en a pas besoin : schema.sql contient déjà tout cela.
-- ============================================================

USE mentoruadb;

-- MySQL 8 ne connaît pas "ADD COLUMN IF NOT EXISTS" : on vérifie via information_schema.
SET @existe := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'utilisateur'
      AND COLUMN_NAME = 'email_recuperation'
);

SET @sql := IF(@existe = 0,
    'ALTER TABLE utilisateur ADD COLUMN email_recuperation VARCHAR(150) NULL AFTER photo',
    'SELECT "colonne email_recuperation déjà présente" AS info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
