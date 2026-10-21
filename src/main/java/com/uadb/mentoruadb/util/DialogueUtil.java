package com.uadb.mentoruadb.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

/**
 * Boîtes de dialogue homogènes : le contenu texte est toujours borné en
 * largeur (retour à la ligne forcé) et défilant quand il peut être long.
 * Sans cela, une longue ligne de notification ou de confirmation produisait
 * une boîte plus large ou plus haute que l'écran, avec les boutons hors vue.
 */
public final class DialogueUtil {

    /** Largeur du contenu : tient dans la fenêtre minimale (900 px). */
    private static final double LARGEUR_MAX_CONTENU = 560.0;
    /** Au-delà, la liste défile plutôt que d'agrandir la boîte. */
    private static final double HAUTEUR_MAX_DEFILEMENT = 400.0;

    private DialogueUtil() {
    }

    /** Pose un contenu texte court dans l'alerte (confirmations, messages courts). */
    public static void contenu(Alert alert, String texte) {
        alert.getDialogPane().setContent(libelle(texte));
    }

    /** Comme contenu(...), mais avec défilement si la liste est longue (notifications, séances). */
    public static void contenuDefilant(Alert alert, String texte) {
        ScrollPane defilement = new ScrollPane(libelle(texte));
        defilement.setFitToWidth(true);
        defilement.setMaxHeight(HAUTEUR_MAX_DEFILEMENT);
        alert.getDialogPane().setContent(defilement);
    }

    private static Label libelle(String texte) {
        Label label = new Label(texte);
        label.setWrapText(true);
        // DialogPane dimensionne la boîte sur la pref du contenu : sans largeur
        // imposée, une longue ligne produisait une boîte plus large que l'écran.
        label.setPrefWidth(LARGEUR_MAX_CONTENU);
        label.setMaxWidth(LARGEUR_MAX_CONTENU);
        return label;
    }
}
