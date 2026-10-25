package com.uadb.mentoruadb.util;

// ==================== DÉBUT IMPORTS ====================
import javafx.scene.control.Label;

import java.util.List;
import java.util.Locale;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Gestion des labels de message des écrans (« messageLabel »).
 *
 * Deux rôles :
 * - rendre le label dynamique : il n'est visible que s'il porte un texte,
 *   sinon il disparaît de la mise en page (fin de la petite boîte rouge
 *   vide qui s'affichait en bas de tous les écrans) ;
 * - colorer le message selon sa nature : erreur en rouge, succès en vert,
 *   avertissement en jaune, information en bleu. La classification est
 *   automatique d'après le texte posé par n'importe quel setText(...), et
 *   les helpers explicites (erreur/succes/avertissement/info) restent
 *   disponibles quand on veut forcer la couleur.
 */
public final class MessageLabel {

    private static final String STYLE_ERREUR = "message-label";
    private static final String STYLE_SUCCES = "succes-label";
    private static final String STYLE_AVERTISSEMENT = "ds-message-warning";
    private static final String STYLE_INFO = "ds-message-info";

    /**
     * Classes ajoutées par la classification automatique. On ne touche jamais aux classes
     * de base de l'écran (« message-label », « acc-message ») : elles portent la mise en
     * page (police, arrondi, padding) et servent de couleur rouge par défaut.
     */
    private static final List<String> CLASSES_SEMANTIQUES =
            List.of(STYLE_SUCCES, STYLE_AVERTISSEMENT, STYLE_INFO);

    /** Mots qui signalent un échec : la priorité la plus haute (ex. « Erreur lors de l'ajout »). */
    private static final List<String> MOTS_ERREUR = List.of(
            "erreur", "impossible", "incorrect", "invalide", "échou", "echec",
            "introuvable", "ne correspond", "n'est pas valide", "n'existe pas", "non reconnu");

    /** Mots qui signalent une action réussie. */
    private static final List<String> MOTS_SUCCES = List.of(
            "succès", "succes", "créé", "créée", "envoyé", "envoyée", "accepté", "acceptée",
            "refusé", "refusée", "ajouté", "ajoutée", "supprimé", "supprimée", "effacé", "effacée",
            "mis à jour", "réinitialisé", "réinitialisée", "enregistré", "enregistrée",
            "partagé", "partagée", "planifié", "planifiée", "annulé", "annulée",
            "confirmé", "confirmée", "réalisé", "réalisée", "rattaché", "rattachée",
            "détaché", "détachée", "terminé", "terminée", "validé", "validée");

    /** Mots qui demandent une action à l'utilisateur ou signalent un manque (pas une panne). */
    private static final List<String> MOTS_AVERTISSEMENT = List.of(
            "sélectionne", "selectionne", "renseigne", "remplis", "choisis", "aucun", "aucune",
            "attention", "doit", "obligatoire", "en cours", "déjà", "seule une", "seul un",
            "pas encore", "peut être");

    private MessageLabel() {
    }

    /**
     * Rend un label de message dynamique : masqué tant que son texte est vide,
     * affiché et coloré dès qu'un texte est posé (par n'importe quel appel à setText).
     * À appeler une fois dans initialize() de chaque contrôleur.
     */
    public static void rendreDynamique(Label label) {
        label.textProperty().addListener((obs, ancien, nouveau) -> {
            majVisibilite(label, nouveau != null && !nouveau.isBlank());
            if (nouveau != null && !nouveau.isBlank()) {
                classer(label, nouveau);
            }
        });
        majVisibilite(label, label.getText() != null && !label.getText().isBlank());
        if (label.getText() != null && !label.getText().isBlank()) {
            classer(label, label.getText());
        }
    }

    /** Affiche un message d'erreur (boîte rouge). */
    public static void erreur(Label label, String texte) {
        afficher(label, texte);
    }

    /** Affiche un message de succès (boîte verte). */
    public static void succes(Label label, String texte) {
        afficher(label, texte, STYLE_SUCCES);
    }

    /** Affiche un avertissement (boîte jaune). */
    public static void avertissement(Label label, String texte) {
        afficher(label, texte, STYLE_AVERTISSEMENT);
    }

    /** Affiche un message informatif (boîte bleue). */
    public static void info(Label label, String texte) {
        afficher(label, texte, STYLE_INFO);
    }

    /** Vide le message et masque le label (changement de rubrique). */
    public static void vider(Label label) {
        label.setText("");
        majVisibilite(label, false);
    }

    /**
     * Pose le texte et force la couleur demandée, sans toucher aux classes de base de
     * l'écran (« message-label », « acc-message ») qui portent la mise en page.
     */
    private static void afficher(Label label, String texte, String... styles) {
        label.setText(texte);
        label.getStyleClass().removeAll(CLASSES_SEMANTIQUES);
        for (String style : styles) {
            if (!label.getStyleClass().contains(style)) {
                label.getStyleClass().add(style);
            }
        }
        majVisibilite(label, true);
    }

    /** Applique la couleur correspondant au texte, sans toucher aux classes de mise en page. */
    private static void classer(Label label, String texte) {
        String minuscules = texte.toLowerCase(Locale.ROOT);
        label.getStyleClass().removeAll(CLASSES_SEMANTIQUES);

        if (contient(minuscules, MOTS_ERREUR)) {
            return;
        }
        if (contient(minuscules, MOTS_SUCCES)) {
            label.getStyleClass().add(STYLE_SUCCES);
            return;
        }
        if (contient(minuscules, MOTS_AVERTISSEMENT)) {
            label.getStyleClass().add(STYLE_AVERTISSEMENT);
            return;
        }
        label.getStyleClass().add(STYLE_INFO);
    }

    private static boolean contient(String texte, List<String> mots) {
        for (String mot : mots) {
            if (texte.contains(mot)) {
                return true;
            }
        }
        return false;
    }

    private static void majVisibilite(Label label, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
    }
}
// ==================== FIN CLASSE ====================
