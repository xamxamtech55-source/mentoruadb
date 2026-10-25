USE referentiel_uadb;

DELETE FROM etudiants
WHERE numero_carte BETWEEN '1900001' AND '1900010'
   OR numero_carte BETWEEN '9100001' AND '9100010';

INSERT INTO etudiants
(numero_carte, email_institutionnel, email_personnel, prenom, nom, filiere_code, niveau_code, statut_inscription)
VALUES
('1900001', 'aminata.ndiaye@uadb.edu.sn', 'aminata.ndiaye@gmail.com', 'Aminata', 'Ndiaye', 'DAA', 'L1', 'ACTIVE'),
('1900002', 'cheikh.diop@uadb.edu.sn', 'cheikh.diop@gmail.com', 'Cheikh', 'Diop', 'DAA', 'L3', 'ACTIVE'),
('1900003', 'mariama.ba@uadb.edu.sn', 'mariama.ba@gmail.com', 'Mariama', 'Ba', 'SID', 'L2', 'ACTIVE'),
('1900004', 'ibrahima.fall@uadb.edu.sn', 'ibrahima.fall@gmail.com', 'Ibrahima', 'Fall', 'SRT', 'L3', 'ACTIVE'),
('1900005', 'awa.sow@uadb.edu.sn', 'awa.sow@gmail.com', 'Awa', 'Sow', 'MED', 'L1', 'SUSPENDUE'),
('1900006', 'mamadou.diallo@uadb.edu.sn', 'mamadou.diallo@gmail.com', 'Mamadou', 'Diallo', 'MED', 'L2', 'ACTIVE'),
('1900007', 'fatou.sene@uadb.edu.sn', 'fatou.sene@gmail.com', 'Fatou', 'Sene', 'FIN', 'L3', 'ACTIVE'),
('1900008', 'ousmane.kane@uadb.edu.sn', 'ousmane.kane@gmail.com', 'Ousmane', 'Kane', 'SCA', 'M1', 'ACTIVE'),
('1900009', 'khady.dieng@uadb.edu.sn', 'khady.dieng@gmail.com', 'Khady', 'Dieng', 'DAR', 'M2', 'ACTIVE'),
('1900010', 'abdoulaye.cisse@uadb.edu.sn', 'abdoulaye.cisse@gmail.com', 'Abdoulaye', 'Cisse', 'ENV', 'L2', 'TERMINEE');
