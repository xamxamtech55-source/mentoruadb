-- =========================================================
-- MIGRATION 003 — demandes de séance + notifications
-- Sûre à relancer plusieurs fois (idempotente) : ne crée que ce qui manque.
--
-- Usage :   mysql -u root -p < database/migration_003_demande_seance_notifications.sql
--
-- Une base neuve n'en a pas besoin : schema.sql contient déjà tout cela.
-- =========================================================

USE mentoruadb;

CREATE TABLE IF NOT EXISTS demande_seance (
                                              id_demande_seance INT AUTO_INCREMENT PRIMARY KEY,
                                              id_demande INT NOT NULL,

                                              date_souhaitee DATE NULL,
                                              message VARCHAR(500) NULL,

    statut ENUM('EN_ATTENTE', 'ACCEPTEE', 'REFUSEE') NOT NULL DEFAULT 'EN_ATTENTE',
    date_creation DATE NOT NULL,

    id_seance INT NULL,

    masque_etudiant TINYINT(1) NOT NULL DEFAULT 0,
    masque_mentor TINYINT(1) NOT NULL DEFAULT 0,

    CONSTRAINT fk_demandeseance_demande FOREIGN KEY (id_demande)
    REFERENCES demande_mentorat(id_demande) ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT fk_demandeseance_seance FOREIGN KEY (id_seance)
    REFERENCES seance(id_seance) ON UPDATE CASCADE ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS notification (
                                            id_notification INT AUTO_INCREMENT PRIMARY KEY,
                                            id_utilisateur INT NOT NULL,

                                            message VARCHAR(500) NOT NULL,
    lue TINYINT(1) NOT NULL DEFAULT 0,
    date_creation DATETIME NOT NULL,

    CONSTRAINT fk_notification_utilisateur FOREIGN KEY (id_utilisateur)
    REFERENCES utilisateur(id_utilisateur) ON UPDATE CASCADE ON DELETE CASCADE
    );