package com.uadb.mentoruadb.dto;

import java.time.LocalDate;

/** Vue enrichie d'une évaluation reçue par un mentor, pour l'affichage (pas une entité de la base). */
public class EvaluationVue {
    private final int idEvaluation;
    private final String nomEtudiant;
    private final String nomMatiere;
    private final int note;
    private final String commentaire;
    private final LocalDate dateEvaluation;

    public EvaluationVue(int idEvaluation, String nomEtudiant, String nomMatiere, int note,
                         String commentaire, LocalDate dateEvaluation) {
        this.idEvaluation = idEvaluation;
        this.nomEtudiant = nomEtudiant;
        this.nomMatiere = nomMatiere;
        this.note = note;
        this.commentaire = commentaire;
        this.dateEvaluation = dateEvaluation;
    }

    public int getIdEvaluation() { return idEvaluation; }
    public String getNomEtudiant() { return nomEtudiant; }
    public String getNomMatiere() { return nomMatiere; }
    public int getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public LocalDate getDateEvaluation() { return dateEvaluation; }
}