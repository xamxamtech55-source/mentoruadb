package com.uadb.mentoruadb.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Envoi de mails SMTP (utilisé pour la réinitialisation de mot de passe).
 *
 * Configuration (par ordre de priorité) :
 *   1. variables d'environnement MENTORUADB_MAIL_HOST, MENTORUADB_MAIL_PORT,
 *      MENTORUADB_MAIL_USER, MENTORUADB_MAIL_PASSWORD, MENTORUADB_MAIL_FROM
 *   2. fichier mail.properties à la racine du projet (clés mail.host, mail.port,
 *      mail.user, mail.password, mail.from) — voir mail.properties.example,
 *      ce fichier est ignoré par Git : il ne doit jamais être commité
 *   3. valeurs par défaut : smtp.gmail.com:587 (mot de passe d'application Gmail)
 *
 * Gmail : la validation en 2 étapes doit être active et il faut créer un
 * « mot de passe d'application » (myaccount.google.com/apppasswords) —
 * le mot de passe normal du compte est refusé.
 */
public final class MailUtil {

    private static final String HOTE_PAR_DEFAUT = "smtp.gmail.com";
    private static final String PORT_PAR_DEFAUT = "587";
    private static final String FICHIER_CONFIG = "mail.properties";

    private static final String hote;
    private static final String port;
    private static final String utilisateur;
    private static final String motDePasse;
    private static final String expediteur;

    static {
        Properties fichier = chargerFichier();
        hote = choisir("MENTORUADB_MAIL_HOST", fichier.getProperty("mail.host"), HOTE_PAR_DEFAUT);
        port = choisir("MENTORUADB_MAIL_PORT", fichier.getProperty("mail.port"), PORT_PAR_DEFAUT);
        utilisateur = choisir("MENTORUADB_MAIL_USER", fichier.getProperty("mail.user"), "");
        motDePasse = choisir("MENTORUADB_MAIL_PASSWORD", fichier.getProperty("mail.password"), "");
        String parDefautExpediteur = utilisateur.isEmpty() ? "no-reply@uadb.edu.sn" : utilisateur;
        expediteur = choisir("MENTORUADB_MAIL_FROM", fichier.getProperty("mail.from"), parDefautExpediteur);
    }

    private MailUtil() {
        // utilitaire : pas d'instanciation
    }

    /** Vrai si un compte expéditeur (identifiants SMTP) est bien configuré. */
    public static boolean estConfigure() {
        return !utilisateur.isEmpty() && !motDePasse.isEmpty();
    }

    /** Envoie un mail en texte brut. Lance MessagingException si le serveur SMTP refuse. */
    public static void envoyer(String destinataire, String sujet, String corps) throws MessagingException {
        Properties proprietes = new Properties();
        proprietes.put("mail.smtp.host", hote);
        proprietes.put("mail.smtp.port", port);
        proprietes.put("mail.smtp.auth", Boolean.toString(!motDePasse.isEmpty()));
        proprietes.put("mail.smtp.starttls.enable", "true");
        proprietes.put("mail.smtp.connectiontimeout", "10000");
        proprietes.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(proprietes, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(utilisateur, motDePasse);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(expediteur));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
        message.setSubject(sujet, "UTF-8");
        message.setText(corps, "UTF-8");

        Transport.send(message);
    }

    private static String choisir(String variableEnv, String valeurFichier, String valeurParDefaut) {
        String env = System.getenv(variableEnv);
        if (env != null && !env.isEmpty()) {
            return env;
        }
        if (valeurFichier != null && !valeurFichier.isBlank()) {
            return valeurFichier.trim();
        }
        return valeurParDefaut;
    }

    private static Properties chargerFichier() {
        Properties proprietes = new Properties();
        Path chemin = Path.of(FICHIER_CONFIG);
        if (Files.isRegularFile(chemin)) {
            try (InputStream in = Files.newInputStream(chemin)) {
                proprietes.load(in);
            } catch (IOException e) {
                System.err.println("Impossible de lire " + FICHIER_CONFIG + " : " + e.getMessage());
            }
        }
        return proprietes;
    }
}
