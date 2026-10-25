package com.uadb.mentoruadb.dto;

// ==================== DÉBUT IMPORTS ====================
import java.time.LocalDate;
import java.time.LocalTime;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Résumé d'une séance (individuelle ou de groupe) pour les tableaux de synthèse
 * du dashboard mentor (pas une entité de la base).
 */
public class SeanceResumeVue {
    private final int idSeance;
    private final String qui;
    private final String nomMatiere;
    private final LocalDate dateSeance;
    private final LocalTime heureDebut;
    private final LocalTime heureFin;
    private final String statut;
    private final String typeSeance;

    public SeanceResumeVue(int idSeance, String qui, String nomMatiere, LocalDate dateSeance,
                            LocalTime heureDebut, LocalTime heureFin, String statut, String typeSeance) {
        this.idSeance = idSeance;
        this.qui = qui;
        this.nomMatiere = nomMatiere;
        this.dateSeance = dateSeance;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.statut = statut;
        this.typeSeance = typeSeance;
    }

    public int getIdSeance() { return idSeance; }
    public String getQui() { return qui; }
    public String getNomMatiere() { return nomMatiere; }
    public LocalDate getDateSeance() { return dateSeance; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public String getStatut() { return statut; }
    public String getTypeSeance() { return typeSeance; }
}
// ==================== FIN CLASSE ====================
