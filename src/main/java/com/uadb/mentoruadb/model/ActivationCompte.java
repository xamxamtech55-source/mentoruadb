package com.uadb.mentoruadb.model;

// ==================== DÉBUT IMPORTS ====================
import java.time.LocalDateTime;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Demande de première activation d'un compte étudiant.
 * Le code à 6 chiffres et le ticket de définition du mot de passe ne sont
 * stockés que hachés (PBKDF2) : cette entité ne contient jamais de secret en
 * clair.
 */
public class ActivationCompte {

    private int idActivation;
    private int idReferentiel;
    private Integer idUtilisateur;
    private String codeHash;
    private boolean codeUtilise;
    /** CODE_EN_ATTENTE, CODE_VALIDE, MOT_DE_PASSE_A_DEFINIR, UTILISE ou ANNULE. */
    private String etat;
    private String ticketHash;
    private String destinataire;
    private LocalDateTime dateExpiration;
    private int tentatives;
    private boolean utilise;
    private LocalDateTime dateCreation;

    public ActivationCompte() {}

    public ActivationCompte(int idActivation, int idReferentiel, Integer idUtilisateur,
                            String codeHash, String ticketHash, String destinataire,
                            LocalDateTime dateExpiration, int tentatives, boolean utilise,
                            LocalDateTime dateCreation) {
        this.idActivation = idActivation;
        this.idReferentiel = idReferentiel;
        this.idUtilisateur = idUtilisateur;
        this.codeHash = codeHash;
        this.ticketHash = ticketHash;
        this.destinataire = destinataire;
        this.dateExpiration = dateExpiration;
        this.tentatives = tentatives;
        this.utilise = utilise;
        this.dateCreation = dateCreation;
    }

    public int getIdActivation() { return idActivation; }
    public void setIdActivation(int idActivation) { this.idActivation = idActivation; }

    public int getIdReferentiel() { return idReferentiel; }
    public void setIdReferentiel(int idReferentiel) { this.idReferentiel = idReferentiel; }

    public Integer getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(Integer idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }

    public boolean isCodeUtilise() { return codeUtilise; }
    public void setCodeUtilise(boolean codeUtilise) { this.codeUtilise = codeUtilise; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getTicketHash() { return ticketHash; }
    public void setTicketHash(String ticketHash) { this.ticketHash = ticketHash; }

    public String getDestinataire() { return destinataire; }
    public void setDestinataire(String destinataire) { this.destinataire = destinataire; }

    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }

    public int getTentatives() { return tentatives; }
    public void setTentatives(int tentatives) { this.tentatives = tentatives; }

    public boolean isUtilise() { return utilise; }
    public void setUtilise(boolean utilise) { this.utilise = utilise; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
// ==================== FIN CLASSE ====================
