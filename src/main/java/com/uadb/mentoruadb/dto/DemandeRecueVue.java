package com.uadb.mentoruadb.dto;

import java.time.LocalDate;

/** Vue enrichie d'une demande reçue par un mentor, pour l'affichage (pas une entité de la base). */
public class DemandeRecueVue {
    private final int idDemande;
    private final String nomEtudiant;
    private final String nomMatiere;
    private final LocalDate dateDemande;
    private final String statut;

    public DemandeRecueVue(int idDemande, String nomEtudiant, String nomMatiere, LocalDate dateDemande, String statut) {
        this.idDemande = idDemande;
        this.nomEtudiant = nomEtudiant;
        this.nomMatiere = nomMatiere;
        this.dateDemande = dateDemande;
        this.statut = statut;
    }

    public int getIdDemande() { return idDemande; }
    public String getNomEtudiant() { return nomEtudiant; }
    public String getNomMatiere() { return nomMatiere; }
    public LocalDate getDateDemande() { return dateDemande; }
    public String getStatut() { return statut; }
}