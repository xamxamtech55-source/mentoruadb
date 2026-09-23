package com.uadb.mentoruadb.model;

import java.time.LocalDate;

/** Entité FICHIER : ressource partagée par un mentor, liée à une matière et/ou une séance précise. */
public class Fichier {
    private int idFichier;
    private int idMentor;
    private Integer idMatiere;
    private Integer idSeance;
    private String nomFichier;
    private String chemin;
    private LocalDate dateUpload;

    public Fichier() {}

    public Fichier(int idFichier, int idMentor, Integer idMatiere, Integer idSeance,
                   String nomFichier, String chemin, LocalDate dateUpload) {
        this.idFichier = idFichier;
        this.idMentor = idMentor;
        this.idMatiere = idMatiere;
        this.idSeance = idSeance;
        this.nomFichier = nomFichier;
        this.chemin = chemin;
        this.dateUpload = dateUpload;
    }

    public int getIdFichier() { return idFichier; }
    public void setIdFichier(int idFichier) { this.idFichier = idFichier; }

    public int getIdMentor() { return idMentor; }
    public void setIdMentor(int idMentor) { this.idMentor = idMentor; }

    public Integer getIdMatiere() { return idMatiere; }
    public void setIdMatiere(Integer idMatiere) { this.idMatiere = idMatiere; }

    public Integer getIdSeance() { return idSeance; }
    public void setIdSeance(Integer idSeance) { this.idSeance = idSeance; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getChemin() { return chemin; }
    public void setChemin(String chemin) { this.chemin = chemin; }

    public LocalDate getDateUpload() { return dateUpload; }
    public void setDateUpload(LocalDate dateUpload) { this.dateUpload = dateUpload; }
}