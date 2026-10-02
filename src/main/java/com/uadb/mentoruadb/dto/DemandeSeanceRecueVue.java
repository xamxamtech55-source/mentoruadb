package com.uadb.mentoruadb.dto;

import java.time.LocalDate;

/** Vue enrichie d'une demande de séance reçue, côté mentor (pas une entité de la base). */
public class DemandeSeanceRecueVue {
    private final int idDemandeSeance;
    private final String nomEtudiant;
    private final String nomMatiere;
    private final LocalDate dateSouhaitee;
    private final String message;
    private final String statut;
    private final LocalDate dateCreation;

    public DemandeSeanceRecueVue(int idDemandeSeance, String nomEtudiant, String nomMatiere, LocalDate dateSouhaitee,
                                 String message, String statut, LocalDate dateCreation) {
        this.idDemandeSeance = idDemandeSeance;
        this.nomEtudiant = nomEtudiant;
        this.nomMatiere = nomMatiere;
        this.dateSouhaitee = dateSouhaitee;
        this.message = message;
        this.statut = statut;
        this.dateCreation = dateCreation;
    }

    public int getIdDemandeSeance() { return idDemandeSeance; }
    public String getNomEtudiant() { return nomEtudiant; }
    public String getNomMatiere() { return nomMatiere; }
    public LocalDate getDateSouhaitee() { return dateSouhaitee; }
    public String getMessage() { return message; }
    public String getStatut() { return statut; }
    public LocalDate getDateCreation() { return dateCreation; }
}