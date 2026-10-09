package com.uadb.mentoruadb.dto;

import java.time.LocalDate;

/** Vue enrichie d'une évaluation donnée par un étudiant, pour l'affichage (pas une entité de la base). */
public class EvaluationEtudiantVue {
    private final int idEvaluation;
    private final String nomMentor;
    private final String nomMatiere;
    private final int note;
    private final String commentaire;
    private final LocalDate dateEvaluation;

    public EvaluationEtudiantVue(int idEvaluation, String nomMentor, String nomMatiere, int note,
                                 String commentaire, LocalDate dateEvaluation) {
        this.idEvaluation = idEvaluation;
        this.nomMentor = nomMentor;
        this.nomMatiere = nomMatiere;
        this.note = note;
        this.commentaire = commentaire;
        this.dateEvaluation = dateEvaluation;
    }

    public int getIdEvaluation() { return idEvaluation; }
    public String getNomMentor() { return nomMentor; }
    public String getNomMatiere() { return nomMatiere; }
    public int getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public LocalDate getDateEvaluation() { return dateEvaluation; }
}
