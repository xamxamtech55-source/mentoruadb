package com.uadb.mentoruadb.service;

import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.dao.ReinitialisationDao;
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.ReinitialisationMotDePasse;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.MailUtil;
import com.uadb.mentoruadb.util.PasswordUtil;
import jakarta.mail.MessagingException;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Réinitialisation de mot de passe en deux temps :
 *   1. envoyerCode(email, numeroCarte)  -> vérifie l'identité puis envoie un code à 6 chiffres par mail
 *   2. reinitialiser(email, code, mdp)  -> vérifie le code (validité, tentatives) puis change le mot de passe
 *
 * Le code n'est stocké que haché (PasswordUtil), expire au bout de DUREE_VALIDITE_MINUTES
 * et n'accepte que TENTATIVES_MAX essais avant d'être invalidé.
 */
public class ReinitialisationService {

    private static final int DUREE_VALIDITE_MINUTES = 15;
    private static final int TENTATIVES_MAX = 5;
    private static final SecureRandom ALEATOIRE = new SecureRandom();

    private static final String MESSAGE_CODE_INVALIDE =
            "Code incorrect ou expiré. Refais une demande depuis « Mot de passe oublié ».";

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final EtudiantDao etudiantDao = new EtudiantDao();
    private final ReinitialisationDao reinitialisationDao = new ReinitialisationDao();

    /**
     * Vérifie email institutionnel + numéro de carte, génère le code, le stocke haché et l'envoie
     * à l'EMAIL DE RÉCUPÉRATION du compte (Gmail ou autre, renseigné dans « Mon profil »).
     * Retourne l'adresse à laquelle le code est parti, ou Optional.empty() si l'identité ne
     * correspond à aucun compte ou si aucun email de récupération n'est renseigné : l'appelant
     * affiche alors exactement le même message que pour un envoi réussi, pour ne rien révéler
     * sur les comptes existants.
     */
    public Optional<String> envoyerCode(String email, String numeroCarte) throws SQLException, MessagingException {
        if (!MailUtil.estConfigure()) {
            throw new MessagingException("Serveur mail non configuré : copie mail.properties.example en "
                    + "mail.properties et renseigne un mot de passe d'application Gmail.");
        }

        Optional<Utilisateur> resultatUtilisateur = utilisateurDao.findByEmail(email.trim());
        if (resultatUtilisateur.isEmpty()) {
            return Optional.empty();
        }
        Optional<Etudiant> resultatEtudiant =
                etudiantDao.findByUtilisateur(resultatUtilisateur.get().getIdUtilisateur());
        if (resultatEtudiant.isEmpty()) {
            return Optional.empty();
        }
        String carteEnregistree = resultatEtudiant.get().getNumeroCarte();
        if (carteEnregistree == null || !carteEnregistree.equals(numeroCarte.trim())) {
            return Optional.empty();
        }

        String emailRecuperation = resultatUtilisateur.get().getEmailRecuperation();
        if (emailRecuperation == null || emailRecuperation.isBlank()) {
            return Optional.empty();
        }

        String code = genererCode();
        reinitialisationDao.creer(
                resultatUtilisateur.get().getIdUtilisateur(),
                PasswordUtil.hacher(code),
                LocalDateTime.now().plusMinutes(DUREE_VALIDITE_MINUTES)
        );

        MailUtil.envoyer(emailRecuperation.trim(), "Réinitialisation de ton mot de passe Mentor UADB",
                corpsDuMail(resultatUtilisateur.get(), code));
        return Optional.of(emailRecuperation.trim());
    }

    /** Vérifie le code puis change le mot de passe. Lance IllegalStateException avec un message à afficher. */
    public void reinitialiser(String email, String codeSaisi, String nouveauMotDePasse,
                              String confirmation) throws SQLException {
        if (codeSaisi.isBlank() || nouveauMotDePasse.isBlank() || confirmation.isBlank()) {
            throw new IllegalStateException("Remplis tous les champs.");
        }
        if (!nouveauMotDePasse.equals(confirmation)) {
            throw new IllegalStateException("Les deux mots de passe ne correspondent pas.");
        }
        if (!codeSaisi.trim().matches("\\d{6}")) {
            throw new IllegalStateException("Le code comporte 6 chiffres.");
        }

        Optional<Utilisateur> resultatUtilisateur = utilisateurDao.findByEmail(email.trim());
        if (resultatUtilisateur.isEmpty()) {
            throw new IllegalStateException(MESSAGE_CODE_INVALIDE);
        }
        Utilisateur utilisateur = resultatUtilisateur.get();

        Optional<ReinitialisationMotDePasse> demandeOpt =
                reinitialisationDao.findDernierNonUtilise(utilisateur.getIdUtilisateur());
        if (demandeOpt.isEmpty()) {
            throw new IllegalStateException(MESSAGE_CODE_INVALIDE);
        }
        ReinitialisationMotDePasse demande = demandeOpt.get();

        if (demande.getTentatives() >= TENTATIVES_MAX) {
            reinitialisationDao.marquerUtilise(demande.getIdReinitialisation());
            throw new IllegalStateException("Trop de tentatives : refais une demande depuis « Mot de passe oublié ».");
        }
        if (demande.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Code expiré. Refais une demande depuis « Mot de passe oublié ».");
        }
        if (!PasswordUtil.verifier(codeSaisi.trim(), demande.getCodeHash())) {
            reinitialisationDao.incrementerTentatives(demande.getIdReinitialisation());
            int restantes = TENTATIVES_MAX - (demande.getTentatives() + 1);
            if (restantes <= 0) {
                reinitialisationDao.marquerUtilise(demande.getIdReinitialisation());
                throw new IllegalStateException("Trop de tentatives : refais une demande depuis « Mot de passe oublié ».");
            }
            throw new IllegalStateException("Code incorrect. Tentatives restantes : " + restantes + ".");
        }

        utilisateur.setMotDePasse(PasswordUtil.hacher(nouveauMotDePasse));
        utilisateurDao.update(utilisateur);
        reinitialisationDao.marquerUtilise(demande.getIdReinitialisation());
    }

    private String genererCode() {
        return String.format("%06d", ALEATOIRE.nextInt(1_000_000));
    }

    private String corpsDuMail(Utilisateur utilisateur, String code) {
        return "Bonjour " + utilisateur.getPrenom() + ",\n\n"
                + "Tu as demandé la réinitialisation de ton mot de passe Mentor UADB.\n\n"
                + "Ton code : " + code + "\n\n"
                + "Ce code expire dans " + DUREE_VALIDITE_MINUTES + " minutes et ne peut être utilisé qu'une fois.\n"
                + "Si tu n'es pas à l'origine de cette demande, ignore ce mail : ton mot de passe reste inchangé.\n\n"
                + "— Mentor UADB, Université Alioune Diop de Bambey";
    }
}
