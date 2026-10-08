-- ============================================================
-- MIGRATION 004 — réinitialisation de mot de passe par mail
-- Sûre à relancer plusieurs fois (idempotente) : ne crée que ce qui manque.
--
-- Usage :   mysql -u root -p < database/migration_004_reinitialisation_mot_de_passe.sql
--
-- Une base neuve n'en a pas besoin : schema.sql contient déjà tout cela.
-- ============================================================

USE mentoruadb;

CREATE TABLE IF NOT EXISTS reinitialisation_mot_de_passe (
                                                              id_reinitialisation INT AUTO_INCREMENT PRIMARY KEY,
                                                              id_utilisateur INT NOT NULL,

    -- Le code à 6 chiffres n'est jamais stocké en clair : hachage PBKDF2 (PasswordUtil).
                                                              code_hash VARCHAR(255) NOT NULL,

                                                              date_expiration DATETIME NOT NULL,
                                                              tentatives TINYINT NOT NULL DEFAULT 0,
                                                              utilise TINYINT(1) NOT NULL DEFAULT 0,
                                                              date_creation DATETIME NOT NULL,

                                                              CONSTRAINT fk_reinit_utilisateur FOREIGN KEY (id_utilisateur)
                                                                  REFERENCES utilisateur(id_utilisateur) ON UPDATE CASCADE ON DELETE CASCADE
);
