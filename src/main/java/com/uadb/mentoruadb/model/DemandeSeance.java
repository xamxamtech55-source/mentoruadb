package com.uadb.mentoruadb.model;

import java.time.LocalDate;

/**
 * Entité DEMANDE_SEANCE : une fois une DemandeMentorat ACCEPTEE, l'étudiant peut demander une
 * séance quand il en a besoin. dateSouhaitee et message sont de simples indications facultatives
 * pour le mentor ; c'est le mentor qui fixe la date et l'heure réelles en acceptant (voir
 * MentoratService.accepterDemandeSeance), ce qui remplit idSeance.
 */
public class DemandeSeance {
    private int idDemandeSeance;
    private int idDemande;
    private LocalDate dateSouhaitee; // optionnel
    private String message;          // optionnel
    private String statut;           // EN_ATTENTE, ACCEPTEE, REFUSEE
    private LocalDate dateCreation;
    private Integer idSeance;        // rempli une fois ACCEPTEE

    public DemandeSeance() {}

    public DemandeSeance(int idDemandeSeance, int idDemande, LocalDate dateSouhaitee, String message,
                         String statut, LocalDate dateCreation, Integer idSeance) {
        this.idDemandeSeance = idDemandeSeance;
        this.idDemande = idDemande;
        this.dateSouhaitee = dateSouhaitee;
        this.message = message;
        this.statut = statut;
        this.dateCreation = dateCreation;
        this.idSeance = idSeance;
    }

    public int getIdDemandeSeance() { return idDemandeSeance; }
    public void setIdDemandeSeance(int idDemandeSeance) { this.idDemandeSeance = idDemandeSeance; }

    public int getIdDemande() { return idDemande; }
    public void setIdDemande(int idDemande) { this.idDemande = idDemande; }

    public LocalDate getDateSouhaitee() { return dateSouhaitee; }
    public void setDateSouhaitee(LocalDate dateSouhaitee) { this.dateSouhaitee = dateSouhaitee; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public Integer getIdSeance() { return idSeance; }
    public void setIdSeance(Integer idSeance) { this.idSeance = idSeance; }
}