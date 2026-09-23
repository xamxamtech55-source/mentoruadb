package com.uadb.mentoruadb.dto;

import java.time.LocalDate;

/** Vue enrichie d'une demande de mentorat, pour l'affichage dans un tableau (pas une entité de la base). */
public class DemandeVue {
    private final int idDemande;
    private final String nomMentor;
    private final String nomMatiere;
    private final LocalDate dateDemande;
    private final String statut;

    public DemandeVue(int idDemande, String nomMentor, String nomMatiere, LocalDate dateDemande, String statut) {
        this.idDemande = idDemande;
        this.nomMentor = nomMentor;
        this.nomMatiere = nomMatiere;
        this.dateDemande = dateDemande;
        this.statut = statut;
    }

    public int getIdDemande() { return idDemande; }
    public String getNomMentor() { return nomMentor; }
    public String getNomMatiere() { return nomMatiere; }
    public LocalDate getDateDemande() { return dateDemande; }
    public String getStatut() { return statut; }
}