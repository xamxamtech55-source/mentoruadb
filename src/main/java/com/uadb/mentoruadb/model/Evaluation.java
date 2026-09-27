package com.uadb.mentoruadb.model;

import java.time.LocalDate;

/** Entité EVALUATION : notée par un étudiant précis, pour une séance (individuelle ou de groupe). */
public class Evaluation {
    private int idEvaluation;
    private int idSeance;
    private int idEtudiant;
    private int note;
    private String commentaire;
    private LocalDate dateEvaluation;

    public Evaluation() {}

    public Evaluation(int idEvaluation, int idSeance, int idEtudiant, int note, String commentaire, LocalDate dateEvaluation) {
        this.idEvaluation = idEvaluation;
        this.idSeance = idSeance;
        this.idEtudiant = idEtudiant;
        this.note = note;
        this.commentaire = commentaire;
        this.dateEvaluation = dateEvaluation;
    }

    public int getIdEvaluation() { return idEvaluation; }
    public void setIdEvaluation(int idEvaluation) { this.idEvaluation = idEvaluation; }

    public int getIdSeance() { return idSeance; }
    public void setIdSeance(int idSeance) { this.idSeance = idSeance; }

    public int getIdEtudiant() { return idEtudiant; }
    public void setIdEtudiant(int idEtudiant) { this.idEtudiant = idEtudiant; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDate getDateEvaluation() { return dateEvaluation; }
    public void setDateEvaluation(LocalDate dateEvaluation) { this.dateEvaluation = dateEvaluation; }
}