package com.uadb.mentoruadb.dto;

/** Mentor suggéré à un étudiant (pas une entité de la base). */
public class MentorSuggestionVue {
    private final int idMentor;
    private final String nomMentor;
    private final String nomFiliere;
    private final String libelleNiveau;
    private final String matieres;
    private final String noteMoyenne;

    public MentorSuggestionVue(int idMentor, String nomMentor, String nomFiliere,
                               String libelleNiveau, String matieres, String noteMoyenne) {
        this.idMentor = idMentor;
        this.nomMentor = nomMentor;
        this.nomFiliere = nomFiliere;
        this.libelleNiveau = libelleNiveau;
        this.matieres = matieres;
        this.noteMoyenne = noteMoyenne;
    }

    public int getIdMentor() { return idMentor; }
    public String getNomMentor() { return nomMentor; }
    public String getNomFiliere() { return nomFiliere; }
    public String getLibelleNiveau() { return libelleNiveau; }
    public String getMatieres() { return matieres; }
    public String getNoteMoyenne() { return noteMoyenne; }
}