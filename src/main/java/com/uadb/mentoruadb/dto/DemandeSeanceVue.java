package com.uadb.mentoruadb.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/** Vue enrichie d'une demande de séance, côté étudiant (pas une entité de la base). */
public class DemandeSeanceVue {
    private final int idDemandeSeance;
    private final String nomMentor;
    private final String nomMatiere;
    private final LocalDate dateSouhaitee;
    private final String message;
    private final String statut;
    private final LocalDate dateSeance;   // rempli seulement si ACCEPTEE
    private final LocalTime heureDebut;   // idem
    private final LocalTime heureFin;     // idem

    public DemandeSeanceVue(int idDemandeSeance, String nomMentor, String nomMatiere, LocalDate dateSouhaitee,
                            String message, String statut, LocalDate dateSeance, LocalTime heureDebut, LocalTime heureFin) {
        this.idDemandeSeance = idDemandeSeance;
        this.nomMentor = nomMentor;
        this.nomMatiere = nomMatiere;
        this.dateSouhaitee = dateSouhaitee;
        this.message = message;
        this.statut = statut;
        this.dateSeance = dateSeance;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public int getIdDemandeSeance() { return idDemandeSeance; }
    public String getNomMentor() { return nomMentor; }
    public String getNomMatiere() { return nomMatiere; }
    public LocalDate getDateSouhaitee() { return dateSouhaitee; }
    public String getMessage() { return message; }
    public String getStatut() { return statut; }
    public LocalDate getDateSeance() { return dateSeance; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
}