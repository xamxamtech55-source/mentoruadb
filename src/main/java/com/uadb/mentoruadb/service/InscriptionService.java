package com.uadb.mentoruadb.service;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gère la création de compte (Utilisateur + profil Etudiant, éventuellement + profil Mentor).
 * Chaque méthode publique écrit dans UNE transaction : si une étape échoue, rien n'est enregistré
 * (pas de compte "orphelin" sans profil Etudiant).
 */
public class InscriptionService {

    private static final String DOMAINE_INSTITUTIONNEL = "@uadb.edu.sn";

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final EtudiantDao etudiantDao = new EtudiantDao();
    private final MentorDao mentorDao = new MentorDao();

    public Etudiant inscrireEtudiant(String nom, String prenom, String email, String motDePasse,
                                     int idFiliere, int idNiveau, String telephone, String numeroCarte) throws SQLException {

        verifierEmail(email);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Etudiant etudiant = creerEtudiant(nom, prenom, email, motDePasse, idFiliere, idNiveau,
                        telephone, numeroCarte, conn);
                conn.commit();
                return etudiant;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Un mentor est d'abord un étudiant ; il demande ensuite à devenir mentor (statut EN_ATTENTE). */
    public Mentor inscrireMentor(String nom, String prenom, String email, String motDePasse,
                                 int idFiliere, int idNiveau, String telephone, String numeroCarte) throws SQLException {

        verifierEmail(email);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Etudiant etudiant = creerEtudiant(nom, prenom, email, motDePasse, idFiliere, idNiveau,
                        telephone, numeroCarte, conn);
                Mentor mentor = new Mentor(0, etudiant.getIdEtudiant(), "EN_ATTENTE");
                mentor = mentorDao.create(mentor, conn);
                conn.commit();
                return mentor;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void verifierEmail(String email) throws SQLException {
        if (!email.toLowerCase().endsWith(DOMAINE_INSTITUTIONNEL)) {
            throw new IllegalArgumentException("L'inscription nécessite une adresse email institutionnelle ("
                    + DOMAINE_INSTITUTIONNEL + ").");
        }
        if (utilisateurDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email.");
        }
    }

    private Etudiant creerEtudiant(String nom, String prenom, String email, String motDePasse,
                                   int idFiliere, int idNiveau, String telephone, String numeroCarte,
                                   Connection conn) throws SQLException {
        Utilisateur utilisateur = new Utilisateur(0, nom, prenom, email, PasswordUtil.hacher(motDePasse),
                "ETUDIANT", "ACTIF", telephone, null);
        utilisateur = utilisateurDao.create(utilisateur, conn);

        Etudiant etudiant = new Etudiant(0, utilisateur.getIdUtilisateur(), idFiliere, idNiveau, numeroCarte);
        return etudiantDao.create(etudiant, conn);
    }
}
