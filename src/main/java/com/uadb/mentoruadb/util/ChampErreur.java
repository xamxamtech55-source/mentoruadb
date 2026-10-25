package com.uadb.mentoruadb.util;

// ==================== DÉBUT IMPORTS ====================
import javafx.scene.Node;
import javafx.scene.Parent;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Met en évidence le champ de saisie responsable d'une erreur (bordure rouge),
 * puis retire la marque dès que l'utilisateur corrige.
 */
public final class ChampErreur {

    private static final String CLASSE = "champ-erreur";

    private ChampErreur() {
    }

    public static void marquer(Node champ) {
        if (champ != null && !champ.getStyleClass().contains(CLASSE)) {
            champ.getStyleClass().add(CLASSE);
        }
    }

    public static void effacer(Node champ) {
        if (champ != null) {
            champ.getStyleClass().remove(CLASSE);
        }
    }

    /** Retire la marque de tous les champs d'un écran (avant une nouvelle soumission). */
    public static void effacerTout(Node racine) {
        if (racine == null) {
            return;
        }
        racine.getStyleClass().remove(CLASSE);
        if (racine instanceof Parent parent) {
            for (Node enfant : parent.getChildrenUnmodifiable()) {
                effacerTout(enfant);
            }
        }
    }
}
// ==================== FIN CLASSE ====================
