package com.uadb.mentoruadb.util;

// ==================== DÉBUT IMPORTS ====================
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Contraintes de saisie des champs numériques posées au fil de la frappe
 * (le service revalide de toute façon : ces filtres améliorent seulement l'ergonomie).
 */
public final class ChampTelephone {

    private ChampTelephone() {
    }

    /**
     * Champ téléphone : seuls les chiffres sont acceptés (et les séparateurs usuels
     * d'un collage), le numéro est formaté « 77 123 45 67 » au fil de la saisie,
     * 9 chiffres au maximum.
     */
    public static void formater(TextField champ) {
        champ.setTextFormatter(new TextFormatter<>(change -> {
            if (!change.isContentChange()) {
                return change;
            }
            String chiffres = change.getControlNewText().replaceAll("\\D", "");
            if (chiffres.startsWith("00221")) {
                chiffres = chiffres.substring(5);
            } else if (chiffres.startsWith("221") && chiffres.length() > 9) {
                chiffres = chiffres.substring(3);
            }
            if (chiffres.length() > 9) {
                return null;
            }
            String formate = Validateur.formaterTelephonePartiel(chiffres);
            change.setRange(0, change.getControlText().length());
            change.setText(formate);
            change.setCaretPosition(formate.length());
            change.setAnchor(formate.length());
            return change;
        }));
    }

    /** Champ limité aux chiffres, avec un nombre maximum de caractères. */
    public static void chiffres(TextField champ, int maximum) {
        champ.setTextFormatter(new TextFormatter<>(change -> {
            if (!change.isContentChange()) {
                return change;
            }
            String chiffres = change.getControlNewText().replaceAll("\\D", "");
            if (chiffres.length() > maximum) {
                chiffres = chiffres.substring(0, maximum);
            }
            if (!chiffres.equals(change.getControlNewText())) {
                change.setRange(0, change.getControlText().length());
                change.setText(chiffres);
                change.setCaretPosition(chiffres.length());
                change.setAnchor(chiffres.length());
            }
            return change;
        }));
    }
}
// ==================== FIN CLASSE ====================
