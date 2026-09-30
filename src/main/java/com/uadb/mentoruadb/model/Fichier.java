package com.uadb.mentoruadb.model;

import java.time.LocalDate;

/**
 * Entité FICHIER : ressource partagée par un mentor, liée à une matière et/ou une séance précise.
 * idEtudiant == null : partagé avec tous les étudiants ayant une demande acceptée pour ce
 *                       mentor + cette matière (comportement historique, "diffusion").
 * idEtudiant renseigné : partagé avec cet étudiant précis uniquement ("partage individuel").
 */
public class Fichier {
    private int idFichier;
    private int idMentor;
    private Integer idMatiere;
    private Integer idSeance;
    private Integer idEtudiant;
    private String nomFichier;
    private String chemin;
    private LocalDate dateUpload;

    public Fichier() {}

    public Fichier(int idFichier, int idMentor, Integer idMatiere, Integer idSeance, Integer idEtudiant,
                   String nomFichier, String chemin, LocalDate dateUpload) {
        this.idFichier = idFichier;
        this.idMentor = idMentor;
        this.idMatiere = idMatiere;
        this.idSeance = idSeance;
        this.idEtudiant = idEtudiant;
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

    public Integer getIdEtudiant() { return idEtudiant; }
    public void setIdEtudiant(Integer idEtudiant) { this.idEtudiant = idEtudiant; }

    /** Vrai si ce fichier est un partage individuel (un seul étudiant), plutôt qu'une diffusion à tous. */
    public boolean estPartageIndividuel() { return idEtudiant != null; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getChemin() { return chemin; }
    public void setChemin(String chemin) { this.chemin = chemin; }

    public LocalDate getDateUpload() { return dateUpload; }
    public void setDateUpload(LocalDate dateUpload) { this.dateUpload = dateUpload; }
}