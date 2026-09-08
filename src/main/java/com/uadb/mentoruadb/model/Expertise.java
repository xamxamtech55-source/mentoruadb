package com.uadb.mentoruadb.model;

/** Entité EXPERTISE : association Mentor <-> Matiere. */
public class Expertise {
    private int idMentor;
    private int idMatiere;

    public Expertise() {}

    public Expertise(int idMentor, int idMatiere) {
        this.idMentor = idMentor;
        this.idMatiere = idMatiere;
    }

    public int getIdMentor() { return idMentor; }
    public void setIdMentor(int idMentor) { this.idMentor = idMentor; }

    public int getIdMatiere() { return idMatiere; }
    public void setIdMatiere(int idMatiere) { this.idMatiere = idMatiere; }
}
