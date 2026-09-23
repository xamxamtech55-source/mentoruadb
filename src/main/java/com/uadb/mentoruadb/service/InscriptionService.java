package com.uadb.mentoruadb.service;

import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.model.Utilisateur;

import java.sql.SQLException;

/**
 * Gère la création de compte (Utilisateur + profil Etudiant ou Mentor).
 * Limite connue : pas de vraie transaction SQL (chaque DAO ouvre/ferme sa
 * connexion) — si le 2e insert échoue, le 1er reste en base. Acceptable
 * pour ce projet, à mentionner dans le rapport comme piste d'amélioration.
 */
public class InscriptionService {

    private static final String DOMAINE_INSTITUTIONNEL = "@uadb.edu.sn";

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final EtudiantDao etudiantDao = new EtudiantDao();
    private final MentorDao mentorDao = new MentorDao();

    public Etudiant inscrireEtudiant(String nom, String prenom, String email, String motDePasse,
                                     int idFiliere, int idNiveau) throws SQLException {

        if (!email.toLowerCase().endsWith(DOMAINE_INSTITUTIONNEL)) {
            throw new IllegalArgumentException("L'inscription nécessite une adresse email institutionnelle ("
                    + DOMAINE_INSTITUTIONNEL + ").");
        }

        if (utilisateurDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email.");
        }

        Utilisateur utilisateur = new Utilisateur(0, nom, prenom, email, motDePasse, "ETUDIANT", "ACTIF");
        utilisateur = utilisateurDao.create(utilisateur);

        Etudiant etudiant = new Etudiant(0, utilisateur.getIdUtilisateur(), idFiliere, idNiveau);
        return etudiantDao.create(etudiant);
    }

    /** Un mentor est d'abord un étudiant ; il demande ensuite à devenir mentor (statut EN_ATTENTE). */
    public Mentor inscrireMentor(String nom, String prenom, String email, String motDePasse,
                                 int idFiliere, int idNiveau) throws SQLException {

        Etudiant etudiant = inscrireEtudiant(nom, prenom, email, motDePasse, idFiliere, idNiveau);

        Mentor mentor = new Mentor(0, etudiant.getIdEtudiant(), "EN_ATTENTE");
        return mentorDao.create(mentor);
    }
}