package com.uadb.mentoruadb.service;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.PasswordUtil;

import java.sql.SQLException;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Changement obligatoire du mot de passe après une première authentification
 * avec un mot de passe provisoire (indicateur utilisateur.must_change_password).
 *
 * Tant que l'indicateur est actif, l'écran de connexion redirige vers cet écran
 * et aucun tableau de bord n'est ouvert.
 */
public class ChangementMotDePasseService {

    /** Longueur minimale d'un mot de passe personnel. */
    public static final int LONGUEUR_MINIMALE = 8;

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();

    /** Validation commune au changement forcé et à la définition du premier mot de passe. */
    public static void validerNouveauMotDePasse(String nouveau, String confirmation) {
        if (nouveau == null || nouveau.isBlank() || confirmation == null || confirmation.isBlank()) {
            throw new IllegalArgumentException("Remplis les deux champs de mot de passe.");
        }
        if (!nouveau.equals(confirmation)) {
            throw new IllegalArgumentException("Les deux mots de passe ne correspondent pas.");
        }
        if (nouveau.length() < LONGUEUR_MINIMALE) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins " + LONGUEUR_MINIMALE + " caractères.");
        }
    }

    /**
     * Vérifie l'ancien mot de passe, enregistre le nouveau haché et lève
     * l'indicateur must_change_password.
     */
    public Utilisateur changerMotDePasse(Utilisateur utilisateur, String ancienMotDePasse,
                                         String nouveauMotDePasse, String confirmation) throws SQLException {
        if (utilisateur == null) {
            throw new IllegalStateException("Aucun compte connecté.");
        }
        validerNouveauMotDePasse(nouveauMotDePasse, confirmation);

        if (!PasswordUtil.verifier(ancienMotDePasse, utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect.");
        }
        if (PasswordUtil.verifier(nouveauMotDePasse, utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'ancien.");
        }

        utilisateur.setMotDePasse(PasswordUtil.hacher(nouveauMotDePasse));
        utilisateur.setMustChangePassword(false);
        utilisateurDao.update(utilisateur);
        return utilisateur;
    }
}
// ==================== FIN CLASSE ====================
