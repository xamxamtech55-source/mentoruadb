package com.uadb.mentoruadb.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Entité SEANCE : créée à partir d'une demande acceptée. */
public class Seance {
    private int idSeance;
    private int idDemande;
    private LocalDate dateSeance;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String statut; // PLANIFIEE, REALISEE, ANNULEE

    public Seance() {}

    public Seance(int idSeance, int idDemande, LocalDate dateSeance,
                   LocalTime heureDebut, LocalTime heureFin, String statut) {
        this.idSeance = idSeance;
        this.idDemande = idDemande;
        this.dateSeance = dateSeance;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.statut = statut;
    }

    public int getIdSeance() { return idSeance; }
    public void setIdSeance(int idSeance) { this.idSeance = idSeance; }

    public int getIdDemande() { return idDemande; }
    public void setIdDemande(int idDemande) { this.idDemande = idDemande; }

    public LocalDate getDateSeance() { return dateSeance; }
    public void setDateSeance(LocalDate dateSeance) { this.dateSeance = dateSeance; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
