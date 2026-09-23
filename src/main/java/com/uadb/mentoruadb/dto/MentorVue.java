package com.uadb.mentoruadb.dto;

/** Vue enrichie d'un mentor pour l'affichage dans les résultats de recherche (pas une entité de la base). */
public class MentorVue {
    private final int idMentor;
    private final String nomMentor;
    private final String nomFiliere;
    private final String libelleNiveau;

    public MentorVue(int idMentor, String nomMentor, String nomFiliere, String libelleNiveau) {
        this.idMentor = idMentor;
        this.nomMentor = nomMentor;
        this.nomFiliere = nomFiliere;
        this.libelleNiveau = libelleNiveau;
    }

    public int getIdMentor() { return idMentor; }
    public String getNomMentor() { return nomMentor; }
    public String getNomFiliere() { return nomFiliere; }
    public String getLibelleNiveau() { return libelleNiveau; }
}