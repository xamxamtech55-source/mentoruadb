package com.uadb.mentoruadb.model;

import java.time.LocalDateTime;

/**
 * Entité REINITIALISATION_MOT_DE_PASSE : demande de réinitialisation en cours.
 * Le code à 6 chiffres n'est stocké que sous forme de haché (PasswordUtil) :
 * la colonne code_hash n'est jamais lisible en clair.
 */
public class ReinitialisationMotDePasse {

    private int idReinitialisation;
    private int idUtilisateur;
    private String codeHash;
    private LocalDateTime dateExpiration;
    private int tentatives;
    private boolean utilise;
    private LocalDateTime dateCreation;

    public ReinitialisationMotDePasse() {}

    public ReinitialisationMotDePasse(int idReinitialisation, int idUtilisateur, String codeHash,
                                      LocalDateTime dateExpiration, int tentatives,
                                      boolean utilise, LocalDateTime dateCreation) {
        this.idReinitialisation = idReinitialisation;
        this.idUtilisateur = idUtilisateur;
        this.codeHash = codeHash;
        this.dateExpiration = dateExpiration;
        this.tentatives = tentatives;
        this.utilise = utilise;
        this.dateCreation = dateCreation;
    }

    public int getIdReinitialisation() { return idReinitialisation; }
    public void setIdReinitialisation(int idReinitialisation) { this.idReinitialisation = idReinitialisation; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }

    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }

    public int getTentatives() { return tentatives; }
    public void setTentatives(int tentatives) { this.tentatives = tentatives; }

    public boolean isUtilise() { return utilise; }
    public void setUtilise(boolean utilise) { this.utilise = utilise; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
