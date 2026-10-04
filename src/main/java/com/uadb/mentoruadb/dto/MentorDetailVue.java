package com.uadb.mentoruadb.dto;

/**
 * Vue enrichie d'un mentor avec son identité et son parcours (pas une entité de la base).
 * Contient l'identité (email, téléphone, n° de carte) pour que l'administrateur puisse
 * vérifier la candidature avant de l'accepter, et consulter la liste complète des mentors.
 */
public class MentorDetailVue {
    private final int idMentor;
    private final String nomComplet;
    private final String email;
    private final String telephone;
    private final String numeroCarte;
    private final String nomFiliere;
    private final String libelleNiveau;
    private final String matieres;
    private final String statutValidation;
    private final String biographie;
    private final String experience;
    private final String modePreference;
    private final Integer nombreMaxMentores;

    public MentorDetailVue(int idMentor, String nomComplet, String email, String telephone,
                             String numeroCarte, String nomFiliere, String libelleNiveau,
                             String matieres, String statutValidation, String biographie,
                             String experience, String modePreference, Integer nombreMaxMentores) {
        this.idMentor = idMentor;
        this.nomComplet = nomComplet;
        this.email = email;
        this.telephone = telephone;
        this.numeroCarte = numeroCarte;
        this.nomFiliere = nomFiliere;
        this.libelleNiveau = libelleNiveau;
        this.matieres = matieres;
        this.statutValidation = statutValidation;
        this.biographie = biographie;
        this.experience = experience;
        this.modePreference = modePreference;
        this.nombreMaxMentores = nombreMaxMentores;
    }

    public int getIdMentor() { return idMentor; }
    public String getNomComplet() { return nomComplet; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getNumeroCarte() { return numeroCarte; }
    public String getNomFiliere() { return nomFiliere; }
    public String getLibelleNiveau() { return libelleNiveau; }
    public String getMatieres() { return matieres; }
    public String getStatutValidation() { return statutValidation; }
    public String getBiographie() { return biographie; }
    public String getExperience() { return experience; }
    public String getModePreference() { return modePreference; }
    public Integer getNombreMaxMentores() { return nombreMaxMentores; }
}