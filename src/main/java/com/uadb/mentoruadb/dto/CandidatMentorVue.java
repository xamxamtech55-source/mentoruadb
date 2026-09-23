package com.uadb.mentoruadb.dto;

/** Vue enrichie d'une candidature mentor en attente, pour l'écran admin (pas une entité de la base). */
public class CandidatMentorVue {
    private final int idMentor;
    private final String nomEtudiant;
    private final String nomFiliere;
    private final String libelleNiveau;
    private final String matieres;
    private final String statutValidation;

    public CandidatMentorVue(int idMentor, String nomEtudiant, String nomFiliere,
                             String libelleNiveau, String matieres, String statutValidation) {
        this.idMentor = idMentor;
        this.nomEtudiant = nomEtudiant;
        this.nomFiliere = nomFiliere;
        this.libelleNiveau = libelleNiveau;
        this.matieres = matieres;
        this.statutValidation = statutValidation;
    }

    public int getIdMentor() { return idMentor; }
    public String getNomEtudiant() { return nomEtudiant; }
    public String getNomFiliere() { return nomFiliere; }
    public String getLibelleNiveau() { return libelleNiveau; }
    public String getMatieres() { return matieres; }
    public String getStatutValidation() { return statutValidation; }
}