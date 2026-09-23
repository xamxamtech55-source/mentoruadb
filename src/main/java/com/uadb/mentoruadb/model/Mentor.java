package com.uadb.mentoruadb.model;

/** Entité MENTOR (étudiant validé). */
public class Mentor {
    private int idMentor;
    private int idEtudiant;
    private String statutValidation;
    private String biographie;
    private String experience;
    private String modePreference;
    private Integer nombreMaxMentores;

    public Mentor() {}

    /** Constructeur historique, sans les nouveaux champs de profil. */
    public Mentor(int idMentor, int idEtudiant, String statutValidation) {
        this(idMentor, idEtudiant, statutValidation, null, null, null, 5);
    }

    public Mentor(int idMentor, int idEtudiant, String statutValidation, String biographie,
                  String experience, String modePreference, Integer nombreMaxMentores) {
        this.idMentor = idMentor;
        this.idEtudiant = idEtudiant;
        this.statutValidation = statutValidation;
        this.biographie = biographie;
        this.experience = experience;
        this.modePreference = modePreference;
        this.nombreMaxMentores = nombreMaxMentores;
    }

    public int getIdMentor() { return idMentor; }
    public void setIdMentor(int idMentor) { this.idMentor = idMentor; }

    public int getIdEtudiant() { return idEtudiant; }
    public void setIdEtudiant(int idEtudiant) { this.idEtudiant = idEtudiant; }

    public String getStatutValidation() { return statutValidation; }
    public void setStatutValidation(String statutValidation) { this.statutValidation = statutValidation; }

    public String getBiographie() { return biographie; }
    public void setBiographie(String biographie) { this.biographie = biographie; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getModePreference() { return modePreference; }
    public void setModePreference(String modePreference) { this.modePreference = modePreference; }

    public Integer getNombreMaxMentores() { return nombreMaxMentores; }
    public void setNombreMaxMentores(Integer nombreMaxMentores) { this.nombreMaxMentores = nombreMaxMentores; }
}