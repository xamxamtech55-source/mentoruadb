package com.uadb.mentoruadb.model;

import java.time.LocalDateTime;

/** Entité NOTIFICATION : message adressé au compte utilisateur d'un étudiant ou d'un mentor. */
public class Notification {
    private int idNotification;
    private int idUtilisateur;
    private String message;
    private boolean lue;
    private LocalDateTime dateCreation;

    public Notification() {}

    public Notification(int idNotification, int idUtilisateur, String message, boolean lue, LocalDateTime dateCreation) {
        this.idNotification = idNotification;
        this.idUtilisateur = idUtilisateur;
        this.message = message;
        this.lue = lue;
        this.dateCreation = dateCreation;
    }

    public int getIdNotification() { return idNotification; }
    public void setIdNotification(int idNotification) { this.idNotification = idNotification; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isLue() { return lue; }
    public void setLue(boolean lue) { this.lue = lue; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}