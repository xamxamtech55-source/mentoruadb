USE mentoruadb;

SET @existe := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'referentiel_etudiant'
      AND COLUMN_NAME = 'id_externe'
);
SET @sql := IF(@existe = 0,
    'ALTER TABLE referentiel_etudiant ADD COLUMN id_externe BIGINT NULL AFTER id_referentiel',
    'SELECT "colonne id_externe deja presente" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @existe := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'referentiel_etudiant'
      AND INDEX_NAME = 'uq_referentiel_externe'
);
SET @sql := IF(@existe = 0,
    'ALTER TABLE referentiel_etudiant ADD CONSTRAINT uq_referentiel_externe UNIQUE (id_externe)',
    'SELECT "index uq_referentiel_externe deja present" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @existe := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'mentoruadb' AND TABLE_NAME = 'referentiel_etudiant'
      AND INDEX_NAME = 'idx_referentiel_etudiant_carte'
);
SET @sql := IF(@existe = 0,
    'CREATE INDEX idx_referentiel_etudiant_carte ON referentiel_etudiant (numero_carte)',
    'SELECT "index idx_referentiel_etudiant_carte deja present" AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
