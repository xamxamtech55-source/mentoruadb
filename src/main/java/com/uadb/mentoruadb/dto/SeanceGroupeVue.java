package com.uadb.mentoruadb.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/** Vue d'une séance de groupe pour le dashboard mentor (pas une entité de la base). */
public class SeanceGroupeVue {
    private final int idSeance;
    private final String nomMatiere;
    private final LocalDate dateSeance;
    private final LocalTime heureDebut;
    private final LocalTime heureFin;
    private final String statut;
    private final String modalite;
    private final String lieu;
    private final int nombreConfirmes;
    private final int nombreInvites;

    public SeanceGroupeVue(int idSeance, String nomMatiere, LocalDate dateSeance, LocalTime heureDebut,
                           LocalTime heureFin, String statut, String modalite, String lieu,
                           int nombreConfirmes, int nombreInvites) {
        this.idSeance = idSeance;
        this.nomMatiere = nomMatiere;
        this.dateSeance = dateSeance;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.statut = statut;
        this.modalite = modalite;
        this.lieu = lieu;
        this.nombreConfirmes = nombreConfirmes;
        this.nombreInvites = nombreInvites;
    }

    public int getIdSeance() { return idSeance; }
    public String getNomMatiere() { return nomMatiere; }
    public LocalDate getDateSeance() { return dateSeance; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public String getStatut() { return statut; }
    public String getModalite() { return modalite; }
    public String getLieu() { return lieu; }
    public int getNombreConfirmes() { return nombreConfirmes; }
    public int getNombreInvites() { return nombreInvites; }

    public String getParticipation() {
        return nombreConfirmes + " / " + nombreInvites + " confirmé(s)";
    }
}