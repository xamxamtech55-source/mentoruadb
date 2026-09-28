package com.uadb.mentoruadb.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hachage et vérification des mots de passe (PBKDF2-HMAC-SHA256, sel aléatoire par mot de passe).
 * Aucune dépendance externe : tout vient du JDK.
 *
 * Format stocké : pbkdf2$<itérations>$<sel base64>$<hash base64>
 *
 * Compatibilité : un mot de passe stocké SANS ce préfixe est considéré comme un ancien mot de passe en clair
 * (données de test, comptes créés avant la correction). Il est encore accepté à la connexion, puis
 * AuthService le remplace immédiatement par son hash.
 */
public final class PasswordUtil {

    private static final String PREFIXE = "pbkdf2";
    private static final String ALGORITHME = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 310_000;
    private static final int TAILLE_SEL = 16;
    private static final int TAILLE_HASH_BITS = 256;
    private static final SecureRandom ALEATOIRE = new SecureRandom();

    private PasswordUtil() {
        // utilitaire : pas d'instanciation
    }

    /** Retourne le mot de passe haché, prêt à être stocké dans utilisateur.mot_de_passe. */
    public static String hacher(String motDePasse) {
        byte[] sel = new byte[TAILLE_SEL];
        ALEATOIRE.nextBytes(sel);
        byte[] hash = deriver(motDePasse, sel, ITERATIONS);
        return PREFIXE + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(sel) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** Vérifie un mot de passe saisi contre la valeur stockée (hash, ou ancien clair). */
    public static boolean verifier(String motDePasseSaisi, String valeurStockee) {
        if (motDePasseSaisi == null || valeurStockee == null) {
            return false;
        }
        if (!estHache(valeurStockee)) {
            // Ancien format en clair : comparaison en temps constant.
            return MessageDigest.isEqual(
                    motDePasseSaisi.getBytes(StandardCharsets.UTF_8),
                    valeurStockee.getBytes(StandardCharsets.UTF_8));
        }
        try {
            String[] parties = valeurStockee.split("\\$");
            int iterations = Integer.parseInt(parties[1]);
            byte[] sel = Base64.getDecoder().decode(parties[2]);
            byte[] attendu = Base64.getDecoder().decode(parties[3]);
            byte[] calcule = deriver(motDePasseSaisi, sel, iterations);
            return MessageDigest.isEqual(attendu, calcule);
        } catch (RuntimeException e) {
            return false; // valeur stockée corrompue
        }
    }

    /** Vrai si la valeur stockée est déjà au format haché. */
    public static boolean estHache(String valeurStockee) {
        return valeurStockee != null && valeurStockee.startsWith(PREFIXE + "$")
                && valeurStockee.split("\\$").length == 4;
    }

    /** Vrai si la valeur stockée doit être re-hachée (ancien clair, ou moins d'itérations que le réglage actuel). */
    public static boolean doitEtreMisAJour(String valeurStockee) {
        if (!estHache(valeurStockee)) {
            return true;
        }
        try {
            return Integer.parseInt(valeurStockee.split("\\$")[1]) < ITERATIONS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static byte[] deriver(String motDePasse, byte[] sel, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(motDePasse.toCharArray(), sel, iterations, TAILLE_HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHME).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Algorithme de hachage indisponible : " + ALGORITHME, e);
        } finally {
            spec.clearPassword();
        }
    }
}
