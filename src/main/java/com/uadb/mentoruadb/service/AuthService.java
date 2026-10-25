package com.uadb.mentoruadb.service;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.dao.ReferentielEtudiantDao;
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.ReferentielEtudiant;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.service.ReferentielUniversitaireService;
import com.uadb.mentoruadb.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Service d'authentification.
 * Les mots de passe sont stockés hachés (PBKDF2, voir PasswordUtil). Un compte encore en clair
 * (données de test ou ancien compte) est accepté une dernière fois puis converti automatiquement.
 */
public class AuthService {

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final EtudiantDao etudiantDao = new EtudiantDao();
    private final ReferentielEtudiantDao referentielDao = new ReferentielEtudiantDao();
    private final ReferentielUniversitaireService referentielUniversitaireService =
            new ReferentielUniversitaireService();

    public Utilisateur connecter(String email, String motDePasse) throws SQLException {
        return connecter(email, null, motDePasse);
    }

    /**
     * Connexion avec numéro de carte.
     *
     * Pour un étudiant ou un mentor, la carte est obligatoire et doit
     * correspondre au compte ; le dossier universitaire lié doit être actif.
     * Les administrateurs n'ont pas de carte : le formulaire unique les
     * authentifie par email + mot de passe.
     */
    public Utilisateur connecter(String email, String numeroCarte, String motDePasse) throws SQLException {
        if (email == null || email.isBlank() || motDePasse == null || motDePasse.isEmpty()) {
            return null;
        }
        Optional<Utilisateur> resultat = utilisateurDao.findByEmail(email.trim().toLowerCase());

        if (resultat.isEmpty()) {
            return null; // aucun compte avec cet email
        }

        Utilisateur utilisateur = resultat.get();
        String valeurStockee = utilisateur.getMotDePasse();

        if (valeurStockee == null || !PasswordUtil.verifier(motDePasse, valeurStockee)) {
            return null; // mot de passe incorrect
        }

        if (!"ADMIN".equals(utilisateur.getRole())) {
            String carte = numeroCarte == null ? "" : numeroCarte.replaceAll("\\s", "");
            if (carte.isEmpty()) {
                return null; // carte obligatoire pour les étudiants et mentors
            }
            Optional<Etudiant> etudiant = etudiantDao.findByUtilisateur(utilisateur.getIdUtilisateur());
            if (etudiant.isEmpty() || etudiant.get().getNumeroCarte() == null
                    || !carte.equals(etudiant.get().getNumeroCarte())) {
                return null;
            }
            if (!dossierUniversitaireActif(etudiant.get())) {
                return null;
            }
        } else {
            // L'administrateur ne doit pas être lié à un dossier étudiant.
            Optional<Etudiant> etudiantAdmin = etudiantDao.findByUtilisateur(utilisateur.getIdUtilisateur());
            if (etudiantAdmin.isPresent()) {
                return null;
            }
        }

        // Un compte désactivé (INACTIF) ne peut plus se connecter : même message que
        // identifiants invalides pour ne pas révéler l'état du compte.
        if (!"ACTIF".equals(utilisateur.getStatut())) {
            return null;
        }

        // Migration transparente : ancien mot de passe en clair -> hash.
        if (PasswordUtil.doitEtreMisAJour(valeurStockee)) {
            utilisateur.setMotDePasse(PasswordUtil.hacher(motDePasse));
            utilisateurDao.update(utilisateur);
        }

        return utilisateur;
    }

    /**
     * Connexion à l'espace d'administration : seuls les comptes ADMIN sont
     * acceptés. Un compte étudiant ou mentor, même avec des identifiants
     * valides, est refusé (retour null) pour ne pas révéler son rôle.
     */
    public Utilisateur connecterAdministrateur(String email, String motDePasse) throws SQLException {
        Utilisateur utilisateur = connecter(email, null, motDePasse);
        if (utilisateur == null || !"ADMIN".equals(utilisateur.getRole())) {
            return null;
        }
        return utilisateur;
    }

    /** Un dossier lié au référentiel doit être ACTIF ; un compte historique non lié reste accepté. */
    private boolean dossierUniversitaireActif(Etudiant etudiant) throws SQLException {
        try {
            if (referentielUniversitaireService.estActif(etudiant.getNumeroCarte(), etudiant.getIdReferentiel())) {
                return true;
            }
        } catch (SQLException e) {
            // En cas d'indisponibilité du référentiel externe, on se rabat sur le miroir local.
        }
        if (etudiant.getIdReferentiel() == null) {
            return true;
        }
        Optional<ReferentielEtudiant> dossier = referentielDao.findById(etudiant.getIdReferentiel());
        return dossier.isPresent() && dossier.get().isInscriptionActive();
    }
}
// ==================== FIN CLASSE ====================
