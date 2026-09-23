-- =========================================================
-- MENTORUADB
-- DONNÉES INITIALES
-- MySQL 8.x
-- =========================================================

USE mentoruadb;


-- =========================================================
-- 1. UFR / INSTITUT
-- =========================================================

INSERT INTO ufr (nom) VALUES
                          (
                              'UFR Sciences Appliquées et Technologies de l''Information et de la Communication'
                          ),
                          (
                              'UFR Santé et Développement Durable'
                          ),
                          (
                              'UFR Économie, Management et Ingénierie Juridique'
                          ),
                          (
                              'Institut Supérieur de Formation Agricole et Rurale'
                          );


-- =========================================================
-- 2. FILIERES
-- =========================================================


-- =========================================================
-- SATIC
-- =========================================================

INSERT INTO filiere (nom, id_ufr)
SELECT
    'Statistiques et Informatique Décisionnelle',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Sciences Appliquées et Technologies de l''Information et de la Communication';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Mathématiques',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Sciences Appliquées et Technologies de l''Information et de la Communication';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Chimie Appliquée',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Sciences Appliquées et Technologies de l''Information et de la Communication';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Physique-Chimie',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Sciences Appliquées et Technologies de l''Information et de la Communication';


-- NOM ACTUEL : Systèmes, Réseaux et Télécoms
INSERT INTO filiere (nom, id_ufr)
SELECT
    'Systèmes, Réseaux et Télécoms',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Sciences Appliquées et Technologies de l''Information et de la Communication';


-- NOM ACTUEL : Développement et Administration d’Applications
INSERT INTO filiere (nom, id_ufr)
SELECT
    'Développement et Administration d''Applications',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Sciences Appliquées et Technologies de l''Information et de la Communication';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Création Multimédia',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Sciences Appliquées et Technologies de l''Information et de la Communication';


-- =========================================================
-- SDD
-- =========================================================

INSERT INTO filiere (nom, id_ufr)
SELECT
    'Médecine',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Santé et Développement Durable';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Santé Communautaire',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Santé et Développement Durable';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Agriculture et Développement Durable',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Santé et Développement Durable';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Environnement',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Santé et Développement Durable';


-- =========================================================
-- ECOMIJ
-- =========================================================

INSERT INTO filiere (nom, id_ufr)
SELECT
    'Économie Appliquée',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Économie, Management et Ingénierie Juridique';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Finance-Comptabilité',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Économie, Management et Ingénierie Juridique';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Management des Organisations',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Économie, Management et Ingénierie Juridique';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Administration Publique',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Économie, Management et Ingénierie Juridique';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Juriste d''Affaires',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Économie, Management et Ingénierie Juridique';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Commerce Électronique et Cybersécurité',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Économie, Management et Ingénierie Juridique';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Management Juridique Environnemental et Foncier',
    id_ufr
FROM ufr
WHERE nom =
      'UFR Économie, Management et Ingénierie Juridique';


-- =========================================================
-- ISFAR
-- =========================================================

INSERT INTO filiere (nom, id_ufr)
SELECT
    'Agriculture',
    id_ufr
FROM ufr
WHERE nom =
      'Institut Supérieur de Formation Agricole et Rurale';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Élevage',
    id_ufr
FROM ufr
WHERE nom =
      'Institut Supérieur de Formation Agricole et Rurale';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Eaux et Forêts',
    id_ufr
FROM ufr
WHERE nom =
      'Institut Supérieur de Formation Agricole et Rurale';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Conseil Agricole et Rural',
    id_ufr
FROM ufr
WHERE nom =
      'Institut Supérieur de Formation Agricole et Rurale';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Développement Agricole et Rural',
    id_ufr
FROM ufr
WHERE nom =
      'Institut Supérieur de Formation Agricole et Rurale';


INSERT INTO filiere (nom, id_ufr)
SELECT
    'Gestion des Aires Protégées et de la Faune',
    id_ufr
FROM ufr
WHERE nom =
      'Institut Supérieur de Formation Agricole et Rurale';


-- =========================================================
-- 3. NIVEAUX
-- =========================================================

INSERT INTO niveau (libelle) VALUES
                                 ('Licence 1'),
                                 ('Licence 2'),
                                 ('Licence 3'),
                                 ('Master 1'),
                                 ('Master 2');


-- =========================================================
-- 4. RATTACHEMENT FILIERE <-> NIVEAU
-- =========================================================


-- =========================================================
-- SATIC
-- =========================================================


-- ---------------------------------------------------------
-- Statistiques et Informatique Décisionnelle
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Statistiques et Informatique Décisionnelle'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Mathématiques
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Mathématiques'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Chimie Appliquée
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Chimie Appliquée'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Physique-Chimie
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Physique-Chimie'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Systèmes, Réseaux et Télécoms
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Systèmes, Réseaux et Télécoms'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Développement et Administration d'Applications
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Développement et Administration d''Applications'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Création Multimédia
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Création Multimédia'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- =========================================================
-- SDD
-- =========================================================


-- ---------------------------------------------------------
-- Médecine
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Médecine'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Santé Communautaire
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Santé Communautaire'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3',
                    'Master 1',
                    'Master 2'
    );


-- ---------------------------------------------------------
-- Agriculture et Développement Durable
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Agriculture et Développement Durable'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Environnement
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Environnement'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- =========================================================
-- ECOMIJ
-- =========================================================


-- ---------------------------------------------------------
-- Économie Appliquée
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Économie Appliquée'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Finance-Comptabilité
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Finance-Comptabilité'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Management des Organisations
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Management des Organisations'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Administration Publique
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Administration Publique'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Juriste d'Affaires
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Juriste d''Affaires'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Commerce Électronique et Cybersécurité
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Commerce Électronique et Cybersécurité'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- ---------------------------------------------------------
-- Management Juridique Environnemental et Foncier
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Management Juridique Environnemental et Foncier'
  AND n.libelle IN (
                    'Licence 1',
                    'Licence 2',
                    'Licence 3'
    );


-- =========================================================
-- ISFAR
-- =========================================================


-- ---------------------------------------------------------
-- Conseil Agricole et Rural
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Conseil Agricole et Rural'
  AND n.libelle = 'Licence 3';


-- ---------------------------------------------------------
-- Développement Agricole et Rural
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Développement Agricole et Rural'
  AND n.libelle IN (
                    'Master 1',
                    'Master 2'
    );


-- ---------------------------------------------------------
-- Gestion des Aires Protégées et de la Faune
-- ---------------------------------------------------------

INSERT INTO filiere_niveau (id_filiere, id_niveau)
SELECT f.id_filiere, n.id_niveau
FROM filiere f
         CROSS JOIN niveau n
WHERE f.nom = 'Gestion des Aires Protégées et de la Faune'
  AND n.libelle IN (
                    'Master 1',
                    'Master 2'
    );


-- =========================================================
-- 5. MATIERES
-- =========================================================

INSERT INTO matiere (nom) VALUES
                              ('Algorithmique'),
                              ('Java'),
                              ('Bases de données'),
                              ('Réseaux'),
                              ('Programmation'),
                              ('Systèmes d''exploitation'),
                              ('Développement Web'),
                              ('Mathématiques'),
                              ('Statistiques'),
                              ('Analyse'),
                              ('Physique'),
                              ('Chimie'),
                              ('Gestion'),
                              ('Économie'),
                              ('Droit'),
                              ('Environnement'),
                              ('Agronomie');


-- =========================================================
-- 6. UTILISATEURS DE TEST
-- =========================================================

INSERT INTO utilisateur
(
    nom,
    prenom,
    email,
    mot_de_passe,
    role,
    statut
)
VALUES
    (
        'Diop',
        'Awa',
        'awa.diop@uadb.edu.sn',
        'password123',
        'ETUDIANT',
        'ACTIF'
    ),
    (
        'Ndiaye',
        'Cheikh',
        'cheikh.ndiaye@uadb.edu.sn',
        'password123',
        'MENTOR',
        'ACTIF'
    ),
    (
        'Fall',
        'Mame',
        'mame.fall@uadb.edu.sn',
        'password123',
        'ADMIN',
        'ACTIF'
    );


-- =========================================================
-- 7. ETUDIANTS DE TEST
-- =========================================================


-- ---------------------------------------------------------
-- Awa : D2A - Licence 2
-- ---------------------------------------------------------

INSERT INTO etudiant
(
    id_utilisateur,
    id_filiere,
    id_niveau
)
SELECT
    u.id_utilisateur,
    f.id_filiere,
    n.id_niveau
FROM utilisateur u
         CROSS JOIN filiere f
         CROSS JOIN niveau n
WHERE u.email = 'awa.diop@uadb.edu.sn'
  AND f.nom = 'Développement et Administration d''Applications'
  AND n.libelle = 'Licence 2';


-- ---------------------------------------------------------
-- Cheikh : D2A - Licence 3
-- ---------------------------------------------------------

INSERT INTO etudiant
(
    id_utilisateur,
    id_filiere,
    id_niveau
)
SELECT
    u.id_utilisateur,
    f.id_filiere,
    n.id_niveau
FROM utilisateur u
         CROSS JOIN filiere f
         CROSS JOIN niveau n
WHERE u.email = 'cheikh.ndiaye@uadb.edu.sn'
  AND f.nom = 'Développement et Administration d''Applications'
  AND n.libelle = 'Licence 3';


-- =========================================================
-- 8. MENTOR
-- =========================================================

INSERT INTO mentor
(
    id_etudiant,
    statut_validation
)
SELECT
    e.id_etudiant,
    'VALIDE'
FROM etudiant e
         JOIN utilisateur u
              ON u.id_utilisateur = e.id_utilisateur
WHERE u.email = 'cheikh.ndiaye@uadb.edu.sn';


-- =========================================================
-- 9. EXPERTISE DU MENTOR
-- =========================================================


-- ---------------------------------------------------------
-- Java
-- ---------------------------------------------------------

INSERT INTO expertise
(
    id_mentor,
    id_matiere
)
SELECT
    m.id_mentor,
    ma.id_matiere
FROM mentor m
         JOIN etudiant e
              ON e.id_etudiant = m.id_etudiant
         JOIN utilisateur u
              ON u.id_utilisateur = e.id_utilisateur
         CROSS JOIN matiere ma
WHERE u.email = 'cheikh.ndiaye@uadb.edu.sn'
  AND ma.nom = 'Java';


-- ---------------------------------------------------------
-- Bases de données
-- ---------------------------------------------------------

INSERT INTO expertise
(
    id_mentor,
    id_matiere
)
SELECT
    m.id_mentor,
    ma.id_matiere
FROM mentor m
         JOIN etudiant e
              ON e.id_etudiant = m.id_etudiant
         JOIN utilisateur u
              ON u.id_utilisateur = e.id_utilisateur
         CROSS JOIN matiere ma
WHERE u.email = 'cheikh.ndiaye@uadb.edu.sn'
  AND ma.nom = 'Bases de données';


-- =========================================================
-- 10. DEMANDE DE MENTORAT
-- =========================================================

INSERT INTO demande_mentorat
(
    id_etudiant,
    id_mentor,
    id_matiere,
    date_demande,
    statut
)
SELECT
    et.id_etudiant,
    m.id_mentor,
    ma.id_matiere,
    '2026-09-01',
    'ACCEPTEE'
FROM etudiant et
         JOIN utilisateur ue
              ON ue.id_utilisateur = et.id_utilisateur

         JOIN mentor m
              ON 1 = 1

         JOIN etudiant em
              ON em.id_etudiant = m.id_etudiant

         JOIN utilisateur um
              ON um.id_utilisateur = em.id_utilisateur

         CROSS JOIN matiere ma

WHERE ue.email = 'awa.diop@uadb.edu.sn'
  AND um.email = 'cheikh.ndiaye@uadb.edu.sn'
  AND ma.nom = 'Java';


-- =========================================================
-- 11. SEANCE
-- =========================================================

INSERT INTO seance
(
    id_demande,
    date_seance,
    heure_debut,
    heure_fin,
    statut
)
SELECT
    d.id_demande,
    '2026-09-05',
    '14:00:00',
    '15:00:00',
    'REALISEE'
FROM demande_mentorat d
         JOIN etudiant e
              ON e.id_etudiant = d.id_etudiant
         JOIN utilisateur u
              ON u.id_utilisateur = e.id_utilisateur
         JOIN matiere ma
              ON ma.id_matiere = d.id_matiere
WHERE u.email = 'awa.diop@uadb.edu.sn'
  AND ma.nom = 'Java';


-- =========================================================
-- 12. EVALUATION
-- =========================================================

INSERT INTO evaluation
(
    id_seance,
    note,
    commentaire,
    date_evaluation
)
SELECT
    s.id_seance,
    5,
    'Séance très claire et bien expliquée.',
    '2026-09-05'
FROM seance s
         JOIN demande_mentorat d
              ON d.id_demande = s.id_demande
         JOIN etudiant e
              ON e.id_etudiant = d.id_etudiant
         JOIN utilisateur u
              ON u.id_utilisateur = e.id_utilisateur
WHERE u.email = 'awa.diop@uadb.edu.sn'
  AND s.date_seance = '2026-09-05';


-- =========================================================
-- FIN DES DONNÉES
-- =========================================================