package com.uadb.mentoruadb.model;

/** Entité MENTOR : un étudiant dont le rôle mentor a été validé. */
public class Mentor {
    private int idMentor;
    private int idEtudiant;
    private String statutValidation; // EN_ATTENTE, VALIDE, REFUSE

    public Mentor() {}

    public Mentor(int idMentor, int idEtudiant, String statutValidation) {
        this.idMentor = idMentor;
        this.idEtudiant = idEtudiant;
        this.statutValidation = statutValidation;
    }

    public int getIdMentor() { return idMentor; }
    public void setIdMentor(int idMentor) { this.idMentor = idMentor; }

    public int getIdEtudiant() { return idEtudiant; }
    public void setIdEtudiant(int idEtudiant) { this.idEtudiant = idEtudiant; }

    public String getStatutValidation() { return statutValidation; }
    public void setStatutValidation(String statutValidation) { this.statutValidation = statutValidation; }
}
