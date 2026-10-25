package com.uadb.mentoruadb.dto;

// ==================== DÉBUT IMPORTS ====================
import java.time.LocalDate;
import java.time.LocalTime;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Vue combinée (séance individuelle ou de groupe) pour les tableaux "Prochaines séances"
 * et "Séances terminées" du dashboard mentor (pas une entité de la base).
 */
public class SeancePlanifieeVue {

    private final int idSeance;
    private final String typeSeance;
    private final String concerne;
    private final String nomMatiere;
    private final LocalDate dateSeance;
    private final LocalTime heureDebut;
    private final LocalTime heureFin;
    private final String statut;

    public SeancePlanifieeVue(int idSeance, String typeSeance, String concerne, String nomMatiere,
                              LocalDate dateSeance, LocalTime heureDebut, LocalTime heureFin, String statut) {
        this.idSeance = idSeance;
        this.typeSeance = typeSeance;
        this.concerne = concerne;
        this.nomMatiere = nomMatiere;
        this.dateSeance = dateSeance;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.statut = statut;
    }

    public static SeancePlanifieeVue depuisIndividuelle(SeanceMentorVue seance) {
        return new SeancePlanifieeVue(seance.getIdSeance(), "Individuelle", seance.getNomEtudiant(),
                seance.getNomMatiere(), seance.getDateSeance(), seance.getHeureDebut(), seance.getHeureFin(),
                seance.getStatut());
    }

    public static SeancePlanifieeVue depuisGroupe(SeanceGroupeVue seance) {
        return new SeancePlanifieeVue(seance.getIdSeance(), "Groupe", "Groupe — " + seance.getParticipation(),
                seance.getNomMatiere(), seance.getDateSeance(), seance.getHeureDebut(), seance.getHeureFin(),
                seance.getStatut());
    }

    public int getIdSeance() { return idSeance; }
    public String getTypeSeance() { return typeSeance; }
    public String getConcerne() { return concerne; }
    public String getNomMatiere() { return nomMatiere; }
    public LocalDate getDateSeance() { return dateSeance; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public String getStatut() { return statut; }
}
// ==================== FIN CLASSE ====================
