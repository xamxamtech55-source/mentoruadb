package com.uadb.mentoruadb.dto;

/**
 * Résultat enrichi d'une recherche de mentor (mentor validé), pour l'affichage en lignes.
 * Ne contient que des données réellement présentes en base (pas de disponibilité inventée) :
 * la « disponibilité » est déduite du mode de préférence et de la capacité (mentorés acceptés / max).
 */
public class MentorRechercheVue {
    private final int idMentor;
    private final String nomMentor;
    private final String nomFiliere;
    private final String libelleNiveau;
    private final String matieres;
    private final Double noteMoyenne;
    private final String modePreference;
    private final Integer nombreMaxMentores;
    private final int nombreMentores;

    public MentorRechercheVue(int idMentor, String nomMentor, String nomFiliere, String libelleNiveau,
                              String matieres, Double noteMoyenne, String modePreference,
                              Integer nombreMaxMentores, int nombreMentores) {
        this.idMentor = idMentor;
        this.nomMentor = nomMentor;
        this.nomFiliere = nomFiliere;
        this.libelleNiveau = libelleNiveau;
        this.matieres = matieres;
        this.noteMoyenne = noteMoyenne;
        this.modePreference = modePreference;
        this.nombreMaxMentores = nombreMaxMentores;
        this.nombreMentores = nombreMentores;
    }

    public int getIdMentor() { return idMentor; }
    public String getNomMentor() { return nomMentor; }
    public String getNomFiliere() { return nomFiliere; }
    public String getLibelleNiveau() { return libelleNiveau; }
    public String getMatieres() { return matieres; }
    public Double getNoteMoyenne() { return noteMoyenne; }
    public String getModePreference() { return modePreference; }
    public Integer getNombreMaxMentores() { return nombreMaxMentores; }
    public int getNombreMentores() { return nombreMentores; }

    /** Note affichable : « 4.5/5 » ou « Pas encore évalué » (jamais de note inventée). */
    public String getNoteTexte() {
        return noteMoyenne != null ? (noteMoyenne + "/5") : "Pas encore évalué";
    }

    /**
     * Disponibilité réelle : mode de préférence et capacité déjà consommée.
     * Ex. « En ligne · 2/5 » ou « Capacité non précisée ». Jamais de valeur inventée.
     */
    public String getDisponibilite() {
        String mode;
        if ("EN_LIGNE".equals(modePreference)) {
            mode = "En ligne";
        } else if ("PRESENTIEL".equals(modePreference)) {
            mode = "Présentiel";
        } else if ("LES_DEUX".equals(modePreference)) {
            mode = "En ligne / présentiel";
        } else {
            mode = "Mode non précisé";
        }

        String capacite = nombreMaxMentores != null
                ? nombreMentores + "/" + nombreMaxMentores + " mentorés"
                : nombreMentores + " mentoré(s)";
        return mode + " · " + capacite;
    }
}
