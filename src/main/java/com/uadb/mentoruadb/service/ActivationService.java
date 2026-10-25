package com.uadb.mentoruadb.service;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.dao.ActivationDao;
import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.dao.ReferentielEtudiantDao;
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.ActivationCompte;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.ReferentielEtudiant;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.MailUtil;
import com.uadb.mentoruadb.util.PasswordUtil;
import com.uadb.mentoruadb.util.Validateur;
import jakarta.mail.MessagingException;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Première connexion d'un étudiant sans compte applicatif.
 *
 * Parcours : demande d'un code (carte + email institutionnel + email personnel
 * enregistré au référentiel) → vérification du code → définition du mot de
 * passe personnel → création transactionnelle du compte → connexion.
 *
 * Le compte n'est créé qu'à la dernière étape. Le code et le ticket de
 * définition du mot de passe ne sont stockés que hachés (PBKDF2).
 */
public class ActivationService {

    public static final int DUREE_CODE_MINUTES = 5;
    public static final int TENTATIVES_MAX = 5;
    public static final int DEMANDES_MAX_PAR_HEURE = 5;
    public static final int INTERVALLE_MINIMAL_SECONDES = 60;
    private static final String DOMAINE_INSTITUTIONNEL = "@uadb.edu.sn";

    /** Résultat de l'étape 1 (messages neutres côté contrôleur). */
    public enum StatutDemande {
        ENVOYE,
        INFORMATIONS_INCORRECTES,
        INSCRIPTION_INACTIVE,
        COMPTE_EXISTANT,
        TROP_DE_DEMANDES
    }

    public record DemandeResultat(StatutDemande statut, String destinataireMasque) {}

    /** Résultat de l'étape 2. */
    public enum StatutVerification {
        TICKET_EMIS
    }

    public record VerificationResultat(StatutVerification statut, String ticket, String carte,
                                       String email, String emailPersonnel, String prenom,
                                       String nom, String filiere, String niveau) {
        public boolean ticketEmis() { return statut == StatutVerification.TICKET_EMIS; }
    }

    @FunctionalInterface
    public interface ExpediteurMail {
        void envoyer(String destinataire, String sujet, String corps) throws MessagingException;
    }

    private final ReferentielEtudiantDao referentielDao = new ReferentielEtudiantDao();
    private final ReferentielUniversitaireService referentielUniversitaireService =
            new ReferentielUniversitaireService();
    private final ActivationDao activationDao = new ActivationDao();
    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final EtudiantDao etudiantDao = new EtudiantDao();

    private final ExpediteurMail expediteur;
    private final boolean configEmailRequise;

    private final SecureRandom aleatoire = new SecureRandom();

    public ActivationService() {
        this(MailUtil::envoyer, true);
    }

    /** Constructeur d'essai : permet d'injecter un faux expéditeur sans SMTP réel. */
    public ActivationService(ExpediteurMail expediteur, boolean configEmailRequise) {
        this.expediteur = expediteur;
        this.configEmailRequise = configEmailRequise;
    }

    // ------------------------------------------------------------------
    // Étape 1 : demande du code
    // ------------------------------------------------------------------

    public DemandeResultat demanderCode(String numeroCarte, String emailInstitutionnel,
                                        String emailPersonnel)
            throws SQLException, MessagingException {
        String carte = normaliserCarte(numeroCarte);
        String email = normaliserEmailInstitutionnel(emailInstitutionnel);
        String personnel = normaliserEmailPersonnel(emailPersonnel);

        Optional<ReferentielEtudiant> dossierOpt;
        try {
            dossierOpt = referentielUniversitaireService.trouverEtSynchroniser(carte, email);
        } catch (IllegalStateException e) {
            return new DemandeResultat(StatutDemande.INFORMATIONS_INCORRECTES, null);
        }
        if (dossierOpt.isEmpty()) {
            return new DemandeResultat(StatutDemande.INFORMATIONS_INCORRECTES, null);
        }
        ReferentielEtudiant dossier = dossierOpt.get();

        if (!dossier.isInscriptionActive()) {
            return new DemandeResultat(StatutDemande.INSCRIPTION_INACTIVE, null);
        }
        if (compteExiste(carte, email)) {
            return new DemandeResultat(StatutDemande.COMPTE_EXISTANT, null);
        }
        if (tropDeDemandes(dossier.getIdReferentiel())) {
            return new DemandeResultat(StatutDemande.TROP_DE_DEMANDES, null);
        }
        if (configEmailRequise && !MailUtil.estConfigure()) {
            throw new IllegalStateException(
                    "L'envoi de mails n'est pas configuré. Contacte l'administrateur.");
        }

        String code = String.format("%06d", aleatoire.nextInt(1_000_000));
        LocalDateTime maintenant = LocalDateTime.now();
        ActivationCompte activation = new ActivationCompte(
                0, dossier.getIdReferentiel(), null,
                PasswordUtil.hacher(code), null, personnel,
                maintenant.plusMinutes(DUREE_CODE_MINUTES), 0, false, maintenant);
        activation.setEtat("CODE_EN_ATTENTE");

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                activationDao.annulerActivesPourReferentiel(dossier.getIdReferentiel(), conn);
                activation = activationDao.create(activation, conn);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }

        try {
            // Envoi du code à l'adresse saisie par l'étudiant
            expediteur.envoyer(personnel, "Code de première connexion MentorUADB",
                    corpsCode(dossier, code));
        } catch (MessagingException e) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                activationDao.annulerActivesPourReferentiel(dossier.getIdReferentiel(), conn);
            }
            throw e;
        }
        return new DemandeResultat(StatutDemande.ENVOYE, masquer(personnel));
    }

    private boolean compteExiste(String carte, String email) throws SQLException {
        return utilisateurDao.findByEmail(email).isPresent()
                || etudiantDao.findByNumeroCarte(carte).isPresent();
    }

    private boolean tropDeDemandes(int idReferentiel) throws SQLException {
        LocalDateTime maintenant = LocalDateTime.now();
        if (activationDao.compterDemandesDepuis(
                idReferentiel, maintenant.minusSeconds(INTERVALLE_MINIMAL_SECONDES)) > 0) {
            return true;
        }
        return activationDao.compterDemandesDepuis(
                idReferentiel, maintenant.minusHours(1)) >= DEMANDES_MAX_PAR_HEURE;
    }

    // ------------------------------------------------------------------
    // Étape 2 : vérification du code (aucun compte créé ici)
    // ------------------------------------------------------------------

    public VerificationResultat verifierCode(String numeroCarte, String emailInstitutionnel, String code)
            throws SQLException {
        String carte = normaliserCarte(numeroCarte);
        String email = normaliserEmailInstitutionnel(emailInstitutionnel);
        if (code == null || !code.trim().matches("\\d{6}")) {
            throw new IllegalArgumentException("Le code doit contenir exactement 6 chiffres.");
        }

        ReferentielEtudiant dossier;
        try {
            dossier = referentielUniversitaireService.trouverEtSynchroniser(carte, email)
                    .orElseThrow(() -> new IllegalStateException(
                            "Code incorrect ou expiré. Demande un nouveau code."));
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Code incorrect ou expiré. Demande un nouveau code.");
        }
        if (!dossier.isInscriptionActive()) {
            throw new IllegalStateException(
                    "Inscription universitaire inactive : la première connexion est impossible.");
        }

        ActivationCompte activation = activationDao.findDerniereActive(dossier.getIdReferentiel())
                .filter(a -> !a.isCodeUtilise())
                .filter(a -> "CODE_EN_ATTENTE".equals(a.getEtat()))
                .orElseThrow(() -> new IllegalStateException(
                        "Code incorrect ou expiré. Demande un nouveau code."));

        if (activation.getDateExpiration().isBefore(LocalDateTime.now())) {
            activationDao.marquerUtilise(activation.getIdActivation());
            throw new IllegalStateException("Code expiré. Demande un nouveau code.");
        }
        if (activation.getTentatives() >= TENTATIVES_MAX) {
            activationDao.marquerUtilise(activation.getIdActivation());
            throw new IllegalStateException("Trop de tentatives. Demande un nouveau code.");
        }
        if (!PasswordUtil.verifier(code.trim(), activation.getCodeHash())) {
            activationDao.incrementerTentatives(activation.getIdActivation());
            int restantes = TENTATIVES_MAX - (activation.getTentatives() + 1);
            if (restantes <= 0) {
                activationDao.marquerUtilise(activation.getIdActivation());
                throw new IllegalStateException("Trop de tentatives. Demande un nouveau code.");
            }
            throw new IllegalStateException("Code incorrect. Tentatives restantes : " + restantes + ".");
        }

        String token = jetonAleatoire();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(DUREE_CODE_MINUTES);
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Utilisateur> utilisateurExistant =
                        utilisateurDao.findByEmail(dossier.getEmailInstitutionnel());
                Optional<Etudiant> etudiantParCarte =
                        etudiantDao.findByNumeroCarte(dossier.getNumeroCarte());
                if (utilisateurExistant.isPresent() || etudiantParCarte.isPresent()) {
                    activationDao.marquerUtilise(activation.getIdActivation(), conn);
                    conn.commit();
                    throw new IllegalStateException(
                            "Un compte existe déjà pour ce dossier. Utilise la page de connexion.");
                }

                activationDao.marquerCodeValide(activation.getIdActivation(), conn);
                activationDao.enregistrerTicket(activation.getIdActivation(),
                        PasswordUtil.hacher(token), expiration, conn);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }

        String ticket = activation.getIdActivation() + "." + token;
        return new VerificationResultat(StatutVerification.TICKET_EMIS, ticket, carte, email,
                dossier.getEmailPersonnel(), dossier.getPrenom(), dossier.getNom(),
                dossier.getNomFiliere(), dossier.getLibelleNiveau());
    }

    // ------------------------------------------------------------------
    // Étape 3 : définition du mot de passe et création du compte
    // ------------------------------------------------------------------

    /** Le ticket a la forme « idActivation.jeton » ; seul le jeton est vérifié par hachage. */
    public Utilisateur definirMotDePasse(String ticket, String nouveauMotDePasse, String confirmation)
            throws SQLException {
        if (ticket == null || !ticket.contains(".")) {
            throw new IllegalStateException("Lien d'activation invalide. Recommence l'activation.");
        }
        int separateur = ticket.indexOf('.');
        int idActivation;
        try {
            idActivation = Integer.parseInt(ticket.substring(0, separateur));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Lien d'activation invalide. Recommence l'activation.");
        }
        String token = ticket.substring(separateur + 1);
        ChangementMotDePasseService.validerNouveauMotDePasse(nouveauMotDePasse, confirmation);

        ActivationCompte activation = activationDao.findById(idActivation)
                .filter(a -> !a.isUtilise())
                .filter(a -> a.getTicketHash() != null)
                .filter(a -> "CODE_VALIDE".equals(a.getEtat())
                        || "MOT_DE_PASSE_A_DEFINIR".equals(a.getEtat()))
                .orElseThrow(() -> new IllegalStateException(
                        "Lien d'activation expiré ou déjà utilisé. Recommence l'activation."));
        if (activation.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Lien d'activation expiré. Recommence l'activation.");
        }
        if (!PasswordUtil.verifier(token, activation.getTicketHash())) {
            throw new IllegalStateException("Lien d'activation invalide. Recommence l'activation.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ReferentielEtudiant dossier = referentielDao
                        .findByIdPourMiseAJour(activation.getIdReferentiel(), conn)
                        .orElseThrow(() -> new IllegalStateException(
                                "Dossier universitaire introuvable. Recommence l'activation."));
                if (!dossier.isInscriptionActive()) {
                    throw new IllegalStateException(
                            "Inscription universitaire inactive : la première connexion est impossible.");
                }
                if (utilisateurDao.findByEmail(dossier.getEmailInstitutionnel()).isPresent()
                        || etudiantDao.findByNumeroCarte(dossier.getNumeroCarte()).isPresent()) {
                    throw new IllegalStateException(
                            "Un compte existe déjà pour ce dossier. Utilise la page de connexion.");
                }

                Utilisateur utilisateur = new Utilisateur(
                        0, dossier.getNom(), dossier.getPrenom(), dossier.getEmailInstitutionnel(),
                        PasswordUtil.hacher(nouveauMotDePasse), "ETUDIANT", "ACTIF");
                utilisateur.setEmailRecuperation(activation.getDestinataire());
                utilisateur.setMustChangePassword(false);
                utilisateurDao.create(utilisateur, conn);

                Etudiant etudiant = new Etudiant(0, utilisateur.getIdUtilisateur(),
                        dossier.getIdFiliere(), dossier.getIdNiveau(), dossier.getNumeroCarte(),
                        dossier.getIdReferentiel());
                etudiantDao.create(etudiant, conn);

                activationDao.associerUtilisateur(activation.getIdActivation(),
                        utilisateur.getIdUtilisateur(), conn);
                activationDao.marquerUtilise(activation.getIdActivation(), conn);
                conn.commit();
                return utilisateur;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ------------------------------------------------------------------
    // Outils
    // ------------------------------------------------------------------

    private static String normaliserCarte(String numeroCarte) {
        String carte = numeroCarte == null ? "" : numeroCarte.replaceAll("\\s", "");
        if (!Validateur.numeroCarteValide(carte)) {
            throw new IllegalArgumentException("Le numéro de carte doit contenir exactement 7 chiffres.");
        }
        return carte;
    }

    private static String normaliserEmailInstitutionnel(String email) {
        String valeur = email == null ? "" : email.trim().toLowerCase();
        if (!Validateur.emailValide(valeur) || !valeur.endsWith(DOMAINE_INSTITUTIONNEL)) {
            throw new IllegalArgumentException("L'email institutionnel doit se terminer par "
                    + DOMAINE_INSTITUTIONNEL + ".");
        }
        return valeur;
    }

    private static String normaliserEmailPersonnel(String email) {
        String valeur = email == null ? "" : email.trim().toLowerCase();
        if (!Validateur.emailValide(valeur)) {
            throw new IllegalArgumentException("L'adresse email personnelle est invalide.");
        }
        return valeur;
    }

    private static String valeur(String texte) {
        return texte == null ? "" : texte.trim().toLowerCase();
    }

    private static String masquer(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }
        int arobase = email.indexOf('@');
        String locale = email.substring(0, arobase);
        String visible = locale.isEmpty() ? "" : locale.substring(0, 1);
        return visible + "***" + email.substring(arobase);
    }

    private String jetonAleatoire() {
        byte[] octets = new byte[32];
        aleatoire.nextBytes(octets);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
    }

    private static String corpsCode(ReferentielEtudiant dossier, String code) {
        return "Bonjour " + dossier.getPrenom() + ",\n\n"
                + "Voici ton code de première connexion MentorUADB : " + code + "\n\n"
                + "Il est valable " + DUREE_CODE_MINUTES + " minutes et ne peut être utilisé qu'une fois.\n"
                + "Si tu n'es pas à l'origine de cette demande, ignore ce message.\n\n"
                + "— MentorUADB";
    }
}
// ==================== FIN CLASSE ====================
