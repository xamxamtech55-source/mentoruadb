package com.uadb.mentoruadb.service;

import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Utilisateur;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service d'authentification.
 * À compléter avec le hachage du mot de passe (ex. BCrypt) avant une
 * mise en production réelle — pour le projet universitaire, comparaison
 * en clair acceptée mais à mentionner comme limite dans le rapport.
 */
public class AuthService {

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();

    public Utilisateur connecter(String email, String motDePasse) throws SQLException {
        Optional<Utilisateur> resultat = utilisateurDao.findByEmail(email);

        if (resultat.isEmpty()) {
            return null; // aucun compte avec cet email
        }

        Utilisateur utilisateur = resultat.get();
        if (!utilisateur.getMotDePasse().equals(motDePasse)) {
            return null; // mot de passe incorrect
        }

        return utilisateur;
    }
}