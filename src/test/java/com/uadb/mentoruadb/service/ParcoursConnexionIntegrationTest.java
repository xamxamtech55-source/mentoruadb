package com.uadb.mentoruadb.service;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Utilisateur;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Scénarios de bout en bout du parcours de connexion / première connexion.
 * Ces tests utilisent la base locale mentoruadb (migration 006 appliquée) et
 * créent puis suppriment leurs propres dossiers fictifs.
 */
class ParcoursConnexionIntegrationTest {

    private static final String MOT_DE_PASSE = "MotDePasse2026!";
    private static final Set<Integer> REFERENTIELS_CREES = new LinkedHashSet<>();
    private static final Set<Integer> UTILISATEURS_CREES = new LinkedHashSet<>();
    private static int compteur = 0;

    private static boolean baseDisponible;
    private static int idFiliere;

    private record Dossier(int id, String carte, String email, String personnel) {}

    private record CodeCapture(String code, String destinataire) {}

    @BeforeAll
    static void verifierBase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            baseDisponible = true;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT f.id_filiere FROM filiere f "
                            + "JOIN filiere_niveau fn ON fn.id_filiere = f.id_filiere "
                            + "JOIN niveau n ON n.id_niveau = fn.id_niveau "
                            + "WHERE n.libelle = 'Licence 2' LIMIT 1")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    idFiliere = rs.next() ? rs.getInt(1) : 0;
                }
            }
        } catch (SQLException e) {
            baseDisponible = false;
        }
        assumeTrue(baseDisponible && idFiliere > 0,
                "Base mentoruadb indisponible : tests d'intégration ignorés.");
    }

    @AfterEach
    void nettoyer() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (int id : REFERENTIELS_CREES) {
                    exec(conn, "DELETE FROM activation_compte WHERE id_referentiel = ?", id);
                    exec(conn, "DELETE FROM etudiant WHERE id_referentiel = ?", id);
                }
                for (int id : UTILISATEURS_CREES) {
                    exec(conn, "DELETE FROM utilisateur WHERE id_utilisateur = ?", id);
                }
                for (int id : REFERENTIELS_CREES) {
                    exec(conn, "DELETE FROM referentiel_etudiant WHERE id_referentiel = ?", id);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                REFERENTIELS_CREES.clear();
                UTILISATEURS_CREES.clear();
            }
        }
    }

    // 1 et 2 : connexion d'un compte existant, mot de passe incorrect.
    @Test
    void connexionCompteExistantEtMotDePasseIncorrect() throws Exception {
        Dossier dossier = creerDossier("ACTIF", "Licence 2");
        Utilisateur compte = activer(dossier);

        AuthService auth = new AuthService();
        Utilisateur connecte = auth.connecter(dossier.email(), dossier.carte(), MOT_DE_PASSE);
        assertNotNull(connecte, "La connexion valide doit réussir");
        assertEquals(compte.getIdUtilisateur(), connecte.getIdUtilisateur());

        assertNull(auth.connecter(dossier.email(), dossier.carte(), "MauvaisMotDePasse!"));
        assertNull(auth.connecter(dossier.email(), "0000000", MOT_DE_PASSE));
        assertNull(auth.connecter(dossier.email(), "", MOT_DE_PASSE),
                "La carte est obligatoire pour un étudiant");
    }

    // 3 à 8 : dossiers invalides, inactifs, personnel différent, compte existant.
    @Test
    void premiereConnexionVerifications() throws Exception {
        ActivationService service = serviceAvecCapture(new AtomicReference<>(), new AtomicReference<>());

        assertCapture(service, "9999999", "inconnu@uadb.edu.sn", "inconnu@gmail.com",
                ActivationService.StatutDemande.INFORMATIONS_INCORRECTES);

        Dossier actif = creerDossier("ACTIF", "Licence 2");
        assertCapture(service, actif.carte(), "autre.email@uadb.edu.sn", actif.personnel(),
                ActivationService.StatutDemande.INFORMATIONS_INCORRECTES);
        assertCapture(service, actif.carte(), actif.email(), "autre.personne@gmail.com",
                ActivationService.StatutDemande.ENVOYE);

        Dossier inactif = creerDossier("INACTIF", "Licence 2");
        assertCapture(service, inactif.carte(), inactif.email(), inactif.personnel(),
                ActivationService.StatutDemande.INSCRIPTION_INACTIVE);

        CodeCapture code = demanderCode(service, actif);
        assertNotNull(code.code());
        assertEquals(actif.personnel(), code.destinataire(),
                "Le code part à l'adresse personnelle saisie");
        assertEquals(0, compterUtilisateurs(actif.email()), "Aucun compte avant validation");

        activerAvecCode(service, actif, code);
        assertCapture(service, actif.carte(), actif.email(), actif.personnel(),
                ActivationService.StatutDemande.COMPTE_EXISTANT);
    }

    // 9 et 10 : code invalide, expiré, déjà utilisé, tentatives dépassées.
    @Test
    void codeInvalideExpireEtTentatives() throws Exception {
        AtomicReference<String> corps = new AtomicReference<>();
        ActivationService service = serviceAvecCapture(new AtomicReference<>(), corps);

        Dossier invalide = creerDossier("ACTIF", "Licence 2");
        CodeCapture code = demanderCode(service, invalide);
        String mauvais = code.code().equals("000000") ? "111111" : "000000";
        IllegalStateException erreur = assertThrows(IllegalStateException.class,
                () -> service.verifierCode(invalide.carte(), invalide.email(), mauvais));
        assertTrue(erreur.getMessage().contains("incorrect"));

        Dossier expire = creerDossier("ACTIF", "Licence 2");
        CodeCapture codeExpire = demanderCode(service, expire);
        execSql("UPDATE activation_compte SET date_expiration = DATE_SUB(NOW(), INTERVAL 1 MINUTE) "
                + "WHERE id_referentiel = ?", expire.id());
        IllegalStateException expiree = assertThrows(IllegalStateException.class,
                () -> service.verifierCode(expire.carte(), expire.email(), codeExpire.code()));
        assertTrue(expiree.getMessage().contains("expiré"));

        Dossier rejoue = creerDossier("ACTIF", "Licence 2");
        CodeCapture codeRejoue = demanderCode(service, rejoue);
        service.verifierCode(rejoue.carte(), rejoue.email(), codeRejoue.code());
        assertThrows(IllegalStateException.class,
                () -> service.verifierCode(rejoue.carte(), rejoue.email(), codeRejoue.code()));

        Dossier tentatives = creerDossier("ACTIF", "Licence 2");
        CodeCapture codeTentatives = demanderCode(service, tentatives);
        String faux = codeTentatives.code().equals("000000") ? "111111" : "000000";
        for (int i = 0; i < ActivationService.TENTATIVES_MAX; i++) {
            assertThrows(IllegalStateException.class,
                    () -> service.verifierCode(tentatives.carte(), tentatives.email(), faux));
        }
        IllegalStateException bloque = assertThrows(IllegalStateException.class,
                () -> service.verifierCode(tentatives.carte(), tentatives.email(),
                        codeTentatives.code()));
        assertTrue(bloque.getMessage().contains("Trop") || bloque.getMessage().contains("expiré"));
    }

    // 11 et 12 : confirmation différente, accès au tableau de bord impossible avant les étapes.
    @Test
    void motDePasseEtAccesDirect() throws Exception {
        ActivationService service = serviceAvecCapture(new AtomicReference<>(), new AtomicReference<>());
        Dossier dossier = creerDossier("ACTIF", "Licence 2");
        CodeCapture code = demanderCode(service, dossier);
        ActivationService.VerificationResultat verification =
                service.verifierCode(dossier.carte(), dossier.email(), code.code());

        assertEquals(0, compterUtilisateurs(dossier.email()),
                "La validation du code ne crée pas encore le compte");

        assertThrows(IllegalArgumentException.class, () -> service.definirMotDePasse(
                verification.ticket(), MOT_DE_PASSE, "AutreMotDePasse!"));
        assertEquals(0, compterUtilisateurs(dossier.email()),
                "Aucun compte sans mot de passe confirmé");

        assertThrows(IllegalStateException.class, () -> service.definirMotDePasse(
                "999999.jeton-invente", MOT_DE_PASSE, MOT_DE_PASSE));
        assertEquals(0, compterUtilisateurs(dossier.email()),
                "Accès direct refusé sans ticket valide");

        Utilisateur cree = service.definirMotDePasse(
                verification.ticket(), MOT_DE_PASSE, MOT_DE_PASSE);
        assertNotNull(cree);
        assertEquals(1, compterUtilisateurs(dossier.email()));
        assertFalse(cree.isMustChangePassword());
        Utilisateur connecte = new AuthService().connecter(
                dossier.email(), dossier.carte(), MOT_DE_PASSE);
        assertNotNull(connecte);
    }

    // 13 : RG20 impossible à contourner depuis le profil étudiant.
    @Test
    void rg20Licence1() throws Exception {
        Dossier l1 = creerDossier("ACTIF", "Licence 1");
        Utilisateur compte = activer(l1);

        Etudiant etudiant = new EtudiantDao().findByUtilisateur(compte.getIdUtilisateur())
                .orElseThrow();
        int idNiveauL2 = idNiveau("Licence 2");
        execSql("UPDATE etudiant SET id_niveau = ? WHERE id_etudiant = ?",
                idNiveauL2, etudiant.getIdEtudiant());
        REFERENTIELS_CREES.add(l1.id());
        UTILISATEURS_CREES.add(compte.getIdUtilisateur());

        EligibiliteService eligibilite = new EligibiliteService();
        String motif = eligibilite.motifIneligibilite(etudiant.getIdEtudiant());
        assertNotNull(motif);
        assertTrue(motif.contains("RG20"));
        assertFalse(eligibilite.estEligibleMentor(etudiant.getIdEtudiant()));

        IllegalStateException refus = assertThrows(IllegalStateException.class,
                () -> new CandidatureMentorService().soumettreCandidature(
                        etudiant.getIdEtudiant(), List.of()));
        assertTrue(refus.getMessage().contains("RG20"));
    }

    // ------------------------------------------------------------------

    private Dossier creerDossier(String statut, String niveau) throws SQLException {
        compteur++;
        String carte = String.format("98%05d", compteur);
        String email = "parcours." + carte + "@uadb.edu.sn";
        String personnel = "parcours." + carte + "@gmail.com";
        int idNiveau = idNiveau(niveau);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO referentiel_etudiant "
                             + "(numero_carte, email_institutionnel, email_personnel, nom, prenom, "
                             + "statut_inscription, id_filiere, id_niveau, annee_academique) "
                             + "VALUES (?, ?, ?, 'Test', 'Parcours', ?, ?, ?, '2025-2026')",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, carte);
            stmt.setString(2, email);
            stmt.setString(3, personnel);
            stmt.setString(4, statut);
            stmt.setInt(5, idFiliere);
            stmt.setInt(6, idNiveau);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                REFERENTIELS_CREES.add(id);
                return new Dossier(id, carte, email, personnel);
            }
        }
    }

    private Utilisateur activer(Dossier dossier) throws Exception {
        ActivationService service = serviceAvecCapture(new AtomicReference<>(), new AtomicReference<>());
        return activerAvecCode(service, dossier, demanderCode(service, dossier));
    }

    private Utilisateur activerAvecCode(ActivationService service, Dossier dossier, CodeCapture code)
            throws Exception {
        ActivationService.VerificationResultat verification =
                service.verifierCode(dossier.carte(), dossier.email(), code.code());
        Utilisateur utilisateur = service.definirMotDePasse(
                verification.ticket(), MOT_DE_PASSE, MOT_DE_PASSE);
        UTILISATEURS_CREES.add(utilisateur.getIdUtilisateur());
        return utilisateur;
    }

    private CodeCapture demanderCode(ActivationService service, Dossier dossier) throws Exception {
        AtomicReference<String> corps = new AtomicReference<>();
        AtomicReference<String> destinataire = new AtomicReference<>();
        ActivationService avecCapture = new ActivationService(
                (dest, sujet, texte) -> {
                    destinataire.set(dest);
                    corps.set(texte);
                }, false);
        ActivationService.DemandeResultat resultat = avecCapture.demanderCode(
                dossier.carte(), dossier.email(), dossier.personnel());
        assertEquals(ActivationService.StatutDemande.ENVOYE, resultat.statut());
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("(\\d{6})").matcher(corps.get());
        assertTrue(matcher.find(), "Le mail doit contenir le code");
        return new CodeCapture(matcher.group(1), destinataire.get());
    }

    private ActivationService serviceAvecCapture(AtomicReference<String> destinataire,
                                                 AtomicReference<String> corps) {
        return new ActivationService((dest, sujet, texte) -> {
            destinataire.set(dest);
            corps.set(texte);
        }, false);
    }

    private void assertCapture(ActivationService service, String carte, String email,
                               String personnel, ActivationService.StatutDemande attendu)
            throws Exception {
        assertEquals(attendu, service.demanderCode(carte, email, personnel).statut());
    }

    private int idNiveau(String libelle) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT n.id_niveau FROM niveau n "
                             + "JOIN filiere_niveau fn ON fn.id_niveau = n.id_niveau "
                             + "WHERE n.libelle = ? AND fn.id_filiere = ? LIMIT 1")) {
            stmt.setString(1, libelle);
            stmt.setInt(2, idFiliere);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Niveau introuvable : " + libelle);
                return rs.getInt(1);
            }
        }
    }

    private int compterUtilisateurs(String email) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM utilisateur WHERE email = ?")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static void exec(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private static void execSql(String sql, int... valeurs) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < valeurs.length; i++) {
                stmt.setInt(i + 1, valeurs[i]);
            }
            stmt.executeUpdate();
        }
    }
}
