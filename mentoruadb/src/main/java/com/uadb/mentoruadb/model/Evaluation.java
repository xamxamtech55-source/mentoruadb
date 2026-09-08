package com.uadb.mentoruadb.model;

import java.time.LocalDate;

/** Entité EVALUATION : liée à une séance et au mentor évalué. */
public class Evaluation {
    private int idEvaluation;
    private int idSeance;
    private int note;
    private String commentaire;
    private LocalDate dateEvaluation;

    public Evaluation() {}

    public Evaluation(int idEvaluation, int idSeance, int note,
                       String commentaire, LocalDate dateEvaluation) {
        this.idEvaluation = idEvaluation;
        this.idSeance = idSeance;
        this.note = note;
        this.commentaire = commentaire;
        this.dateEvaluation = dateEvaluation;
    }

    public int getIdEvaluation() { return idEvaluation; }
    public void setIdEvaluation(int idEvaluation) { this.idEvaluation = idEvaluation; }

    public int getIdSeance() { return idSeance; }
    public void setIdSeance(int idSeance) { this.idSeance = idSeance; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDate getDateEvaluation() { return dateEvaluation; }
    public void setDateEvaluation(LocalDate dateEvaluation) { this.dateEvaluation = dateEvaluation; }
}
