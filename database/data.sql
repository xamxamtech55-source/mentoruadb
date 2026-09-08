-- =========================================================
-- MentorUADB — Jeu de données de test
-- =========================================================
USE mentoruadb;

INSERT INTO ufr (nom) VALUES
('UFR Sciences et Technologies'),
('UFR Sciences Économiques et Sociales');

INSERT INTO filiere (nom, id_ufr) VALUES
('Informatique', 1),
('Mathématiques', 1),
('Gestion', 2);

INSERT INTO niveau (libelle) VALUES
('Licence 1'), ('Licence 2'), ('Licence 3'), ('Master 1'), ('Master 2');

INSERT INTO matiere (nom) VALUES
('Algorithmique'), ('Java'), ('Bases de données'), ('Réseaux'), ('Analyse mathématique');

-- Comptes utilisateurs (mot de passe en clair ici uniquement pour le jeu de test)
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role, statut) VALUES
('Diop',  'Awa',    'awa.diop@uadb.edu.sn',    'password123', 'ETUDIANT', 'ACTIF'),
('Ndiaye','Cheikh',  'cheikh.ndiaye@uadb.edu.sn','password123', 'ETUDIANT', 'ACTIF'),
('Fall',  'Mame',   'mame.fall@uadb.edu.sn',    'password123', 'ADMIN',    'ACTIF');

INSERT INTO etudiant (id_utilisateur, id_filiere, id_niveau) VALUES
(1, 1, 2),  -- Awa Diop, Informatique, Licence 2
(2, 1, 3);  -- Cheikh Ndiaye, Informatique, Licence 3

-- Cheikh devient mentor (validé)
INSERT INTO mentor (id_etudiant, statut_validation) VALUES
(2, 'VALIDE');

INSERT INTO expertise (id_mentor, id_matiere) VALUES
(1, 2), -- Cheikh -> Java
(1, 3); -- Cheikh -> Bases de données

INSERT INTO demande_mentorat (id_etudiant, id_mentor, id_matiere, date_demande, statut) VALUES
(1, 1, 2, '2026-09-01', 'ACCEPTEE');

INSERT INTO seance (id_demande, date_seance, heure_debut, heure_fin, statut) VALUES
(1, '2026-09-05', '14:00:00', '15:00:00', 'REALISEE');

INSERT INTO evaluation (id_seance, note, commentaire, date_evaluation) VALUES
(1, 5, 'Séance très claire et bien expliquée.', '2026-09-05');
