package com.uadb.mentoruadb.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Entité SEANCE : individuelle (liée à une demande) ou de groupe (liée à un mentor + une matière). */
public class Seance {
    private int idSeance;
    private Integer idDemande;      // rempli pour une séance INDIVIDUELLE, null pour une séance GROUPE
    private String typeSeance;      // INDIVIDUELLE ou GROUPE
    private Integer idMentor;       // rempli pour une séance GROUPE, null pour une séance INDIVIDUELLE
    private Integer idMatiere;      // idem
    private LocalDate dateSeance;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String statut;
    private String modalite;        // EN_LIGNE ou PRESENTIEL
    private String lieu;            // salle ou lien, selon la modalité

    public Seance() {}

    public Seance(int idSeance, Integer idDemande, String typeSeance, Integer idMentor, Integer idMatiere,
                  LocalDate dateSeance, LocalTime heureDebut, LocalTime heureFin, String statut,
                  String modalite, String lieu) {
        this.idSeance = idSeance;
        this.idDemande = idDemande;
        this.typeSeance = typeSeance;
        this.idMentor = idMentor;
        this.idMatiere = idMatiere;
        this.dateSeance = dateSeance;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.statut = statut;
        this.modalite = modalite;
        this.lieu = lieu;
    }

    /** Constructeur pratique pour une séance individuelle (comme avant l'extension groupe). */
    public static Seance individuelle(int idSeance, int idDemande, LocalDate dateSeance,
                                      LocalTime heureDebut, LocalTime heureFin, String statut,
                                      String modalite, String lieu) {
        return new Seance(idSeance, idDemande, "INDIVIDUELLE", null, null,
                dateSeance, heureDebut, heureFin, statut, modalite, lieu);
    }

    /** Constructeur pratique pour une séance de groupe. */
    public static Seance groupe(int idSeance, int idMentor, int idMatiere, LocalDate dateSeance,
                                LocalTime heureDebut, LocalTime heureFin, String statut,
                                String modalite, String lieu) {
        return new Seance(idSeance, null, "GROUPE", idMentor, idMatiere,
                dateSeance, heureDebut, heureFin, statut, modalite, lieu);
    }

    public int getIdSeance() { return idSeance; }
    public void setIdSeance(int idSeance) { this.idSeance = idSeance; }

    public Integer getIdDemande() { return idDemande; }
    public void setIdDemande(Integer idDemande) { this.idDemande = idDemande; }

    public String getTypeSeance() { return typeSeance; }
    public void setTypeSeance(String typeSeance) { this.typeSeance = typeSeance; }

    public Integer getIdMentor() { return idMentor; }
    public void setIdMentor(Integer idMentor) { this.idMentor = idMentor; }

    public Integer getIdMatiere() { return idMatiere; }
    public void setIdMatiere(Integer idMatiere) { this.idMatiere = idMatiere; }

    public LocalDate getDateSeance() { return dateSeance; }
    public void setDateSeance(LocalDate dateSeance) { this.dateSeance = dateSeance; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getModalite() { return modalite; }
    public void setModalite(String modalite) { this.modalite = modalite; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
}