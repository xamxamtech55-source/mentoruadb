package com.uadb.mentoruadb.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/** Vue enrichie d'une séance, pour l'affichage dans un tableau (pas une entité de la base). */
public class SeanceVue {
    private final int idSeance;
    private final String nomMentor;
    private final String nomMatiere;
    private final LocalDate dateSeance;
    private final LocalTime heureDebut;
    private final LocalTime heureFin;
    private final String statut;

    public SeanceVue(int idSeance, String nomMentor, String nomMatiere, LocalDate dateSeance,
                     LocalTime heureDebut, LocalTime heureFin, String statut) {
        this.idSeance = idSeance;
        this.nomMentor = nomMentor;
        this.nomMatiere = nomMatiere;
        this.dateSeance = dateSeance;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.statut = statut;
    }

    public int getIdSeance() { return idSeance; }
    public String getNomMentor() { return nomMentor; }
    public String getNomMatiere() { return nomMatiere; }
    public LocalDate getDateSeance() { return dateSeance; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public String getStatut() { return statut; }
}