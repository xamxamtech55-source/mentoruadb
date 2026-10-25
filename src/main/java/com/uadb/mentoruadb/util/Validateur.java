package com.uadb.mentoruadb.util;

// ==================== DÉBUT CLASSE ====================
/**
 * Règles de validation des données saisies par l'utilisateur.
 * Centralisées ici pour que les contrôleurs (retour immédiat) et les services
 * (règle métier) appliquent exactement la même règle.
 */
public final class Validateur {

    /** Préfixes de téléphone valides au Sénégal (mobile 70/75/76/77/78, fixe 30/33). */
    private static final String PREFIXES_TELEPHONE = "70|75|76|77|78|30|33";

    private Validateur() {
    }

    /** Un numéro de carte d'étudiant comporte exactement 7 chiffres. */
    public static boolean numeroCarteValide(String numeroCarte) {
        return numeroCarte != null && numeroCarte.trim().matches("\\d{7}");
    }

    /** Un email doit avoir la forme locale@domaine.tld (la règle @uadb.edu.sn est vérifiée par le service). */
    public static boolean emailValide(String email) {
        return email != null && email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    /**
     * Numéro sénégalais à 9 chiffres. Accepte les séparateurs courants et le préfixe
     * international : « 77 123 45 67 », « 77-123-45-67 », « +221 77 123 45 67 ».
     */
    public static boolean telephoneValide(String telephone) {
        String chiffres = normaliserTelephone(telephone);
        return chiffres.matches("^(?:" + PREFIXES_TELEPHONE + ")\\d{7}$");
    }

    /**
     * Ne garde que les chiffres et retire le préfixe international 221, pour stocker
     * toujours 9 chiffres (« 77 123 45 67 » -> « 771234567 »).
     */
    public static String normaliserTelephone(String telephone) {
        if (telephone == null) {
            return "";
        }
        String chiffres = telephone.replaceAll("\\D", "");
        if (chiffres.startsWith("00")) {
            chiffres = chiffres.substring(2);
        }
        if (chiffres.startsWith("221") && chiffres.length() == 12) {
            chiffres = chiffres.substring(3);
        }
        return chiffres;
    }

    /** Formate un numéro à 9 chiffres pour l'affichage : 77 123 45 67. */
    public static String formaterTelephone(String telephone) {
        String chiffres = normaliserTelephone(telephone);
        if (chiffres.length() != 9) {
            return telephone == null ? "" : telephone.trim();
        }
        return formaterTelephonePartiel(chiffres);
    }

    /**
     * Formate progressivement de 0 à 9 chiffres, pour la saisie au fil de la frappe :
     * « 7 » -> « 7 », « 771 » -> « 77 1 », « 771234567 » -> « 77 123 45 67 ».
     */
    public static String formaterTelephonePartiel(String chiffres) {
        if (chiffres == null) {
            return "";
        }
        StringBuilder formate = new StringBuilder();
        for (int i = 0; i < chiffres.length() && i < 9; i++) {
            if (i == 2 || i == 5 || i == 7) {
                formate.append(' ');
            }
            formate.append(chiffres.charAt(i));
        }
        return formate.toString();
    }
}
// ==================== FIN CLASSE ====================
