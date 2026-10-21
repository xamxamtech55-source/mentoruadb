package com.uadb.mentoruadb.util;

import javafx.scene.control.Label;

/**
 * Gestion des labels de message des écrans (« messageLabel »).
 *
 * Deux rôles :
 * - rendre le label dynamique : il n'est visible que s'il porte un texte,
 *   sinon il disparaît de la mise en page (fin de la petite boîte rouge
 *   vide qui s'affichait en bas de tous les écrans) ;
 * - offrir des méthodes d'affichage homogènes (erreur / succès / info)
 *   qui appliquent le bon style et gèrent la visibilité.
 */
public final class MessageLabel {

    private MessageLabel() {
    }

    /**
     * Rend un label de message dynamique : masqué tant que son texte est vide,
     * affiché dès qu'un texte est posé (par n'importe quel appel à setText).
     * À appeler une fois dans initialize() de chaque contrôleur.
     */
    public static void rendreDynamique(Label label) {
        label.textProperty().addListener((obs, ancien, nouveau) ->
                majVisibilite(label, nouveau != null && !nouveau.isBlank()));
        majVisibilite(label, label.getText() != null && !label.getText().isBlank());
    }

    /** Affiche un message d'erreur (boîte rouge). */
    public static void erreur(Label label, String texte) {
        afficher(label, texte, "message-label");
    }

    /** Affiche un message de succès (boîte verte). */
    public static void succes(Label label, String texte) {
        afficher(label, texte, "succes-label");
    }

    /** Affiche un message informatif (boîte bleue). */
    public static void info(Label label, String texte) {
        afficher(label, texte, "ds-message", "ds-message-info");
    }

    /** Vide le message et masque le label (changement de rubrique). */
    public static void vider(Label label) {
        label.setText("");
        majVisibilite(label, false);
    }

    private static void afficher(Label label, String texte, String... styles) {
        label.getStyleClass().setAll(styles);
        label.setText(texte);
        majVisibilite(label, true);
    }

    private static void majVisibilite(Label label, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
    }
}
