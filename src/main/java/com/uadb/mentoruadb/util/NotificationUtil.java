package com.uadb.mentoruadb.util;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.NotificationDao;
import com.uadb.mentoruadb.model.Notification;
import javafx.scene.control.Alert;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Affichage des notifications d'un compte : le contenu complet (date + message)
 * est présenté dans une boîte de dialogue ; à la fermeture de celle-ci, chaque
 * notification affichée est marquée comme lue (comportement « consultation =
 * lecture » du projet). Aucune navigation n'est inventée : les notifications
 * ne portent pas de référence vers une ressource métier.
 */
public final class NotificationUtil {

    private static final DateTimeFormatter FORMATEUR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private NotificationUtil() {
    }

    /**
     * Affiche les notifications dans une boîte de dialogue puis marque chacune
     * d'elles comme lue. Retourne true si des notifications ont été affichées.
     */
    public static boolean afficherEtMarquerLues(List<Notification> notifications, NotificationDao dao) {
        if (notifications == null || notifications.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucune nouvelle notification.");
            alert.setTitle("Notifications");
            alert.setHeaderText(null);
            alert.showAndWait();
            return false;
        }

        StringBuilder texte = new StringBuilder();
        for (Notification n : notifications) {
            texte.append("• ").append(n.getDateCreation().format(FORMATEUR))
                    .append(" — ").append(n.getMessage()).append('\n');
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText(notifications.size() + " notification(s)");
        alert.setContentText(null);
        DialogueUtil.contenuDefilant(alert, texte.toString());
        alert.showAndWait();

        // La consultation est faite : chaque notification affichée est lue.
        for (Notification n : notifications) {
            try {
                dao.marquerLue(n.getIdNotification());
            } catch (SQLException e) {
                // Une notification non marquée réapparaîtra au prochain affichage.
            }
        }
        return true;
    }
}
// ==================== FIN CLASSE ====================
