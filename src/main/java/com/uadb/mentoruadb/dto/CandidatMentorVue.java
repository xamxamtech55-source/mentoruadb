package com.uadb.mentoruadb.dto;

/** Vue enrichie d'une candidature mentor en attente, pour l'écran admin (pas une entité de la base). */
public class CandidatMentorVue {
    private final int idMentor;
    private final String nomEtudiant;
    private final String nomFiliere;
    private final String libelleNiveau;
    private final String matieres;
    private final String statutValidation;
    private final String biographie;
    private final String experience;
    private final String modePreference;

    public CandidatMentorVue(int idMentor, String nomEtudiant, String nomFiliere,
                             String libelleNiveau, String matieres, String statutValidation,
                             String biographie, String experience, String modePreference) {
        this.idMentor = idMentor;
        this.nomEtudiant = nomEtudiant;
        this.nomFiliere = nomFiliere;
        this.libelleNiveau = libelleNiveau;
        this.matieres = matieres;
        this.statutValidation = statutValidation;
        this.biographie = biographie;
        this.experience = experience;
        this.modePreference = modePreference;
    }

    public int getIdMentor() { return idMentor; }
    public String getNomEtudiant() { return nomEtudiant; }
    public String getNomFiliere() { return nomFiliere; }
    public String getLibelleNiveau() { return libelleNiveau; }
    public String getMatieres() { return matieres; }
    public String getStatutValidation() { return statutValidation; }
    public String getBiographie() { return biographie; }
    public String getExperience() { return experience; }
    public String getModePreference() { return modePreference; }
}