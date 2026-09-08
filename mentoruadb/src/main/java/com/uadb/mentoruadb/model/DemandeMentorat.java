package com.uadb.mentoruadb.model;

import java.time.LocalDate;

/** Entité DEMANDE_MENTORAT. */
public class DemandeMentorat {
    private int idDemande;
    private int idEtudiant;
    private int idMentor;
    private int idMatiere;
    private LocalDate dateDemande;
    private String statut; // EN_ATTENTE, ACCEPTEE, REFUSEE

    public DemandeMentorat() {}

    public DemandeMentorat(int idDemande, int idEtudiant, int idMentor, int idMatiere,
                            LocalDate dateDemande, String statut) {
        this.idDemande = idDemande;
        this.idEtudiant = idEtudiant;
        this.idMentor = idMentor;
        this.idMatiere = idMatiere;
        this.dateDemande = dateDemande;
        this.statut = statut;
    }

    public int getIdDemande() { return idDemande; }
    public void setIdDemande(int idDemande) { this.idDemande = idDemande; }

    public int getIdEtudiant() { return idEtudiant; }
    public void setIdEtudiant(int idEtudiant) { this.idEtudiant = idEtudiant; }

    public int getIdMentor() { return idMentor; }
    public void setIdMentor(int idMentor) { this.idMentor = idMentor; }

    public int getIdMatiere() { return idMatiere; }
    public void setIdMatiere(int idMatiere) { this.idMatiere = idMatiere; }

    public LocalDate getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDate dateDemande) { this.dateDemande = dateDemande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
