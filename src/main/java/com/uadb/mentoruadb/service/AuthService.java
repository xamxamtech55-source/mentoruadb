package com.uadb.mentoruadb.service;

import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service d'authentification.
 * Les mots de passe sont stockés hachés (PBKDF2, voir PasswordUtil). Un compte encore en clair
 * (données de test ou ancien compte) est accepté une dernière fois puis converti automatiquement.
 */
public class AuthService {

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();

    public Utilisateur connecter(String email, String motDePasse) throws SQLException {
        Optional<Utilisateur> resultat = utilisateurDao.findByEmail(email);

        if (resultat.isEmpty()) {
            return null; // aucun compte avec cet email
        }

        Utilisateur utilisateur = resultat.get();
        String valeurStockee = utilisateur.getMotDePasse();

        if (!PasswordUtil.verifier(motDePasse, valeurStockee)) {
            return null; // mot de passe incorrect
        }

        // Migration transparente : ancien mot de passe en clair -> hash.
        if (PasswordUtil.doitEtreMisAJour(valeurStockee)) {
            utilisateur.setMotDePasse(PasswordUtil.hacher(motDePasse));
            utilisateurDao.update(utilisateur);
        }

        return utilisateur;
    }
}
