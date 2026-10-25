package com.uadb.mentoruadb.model;

// ==================== DÉBUT IMPORTS ====================
import java.time.LocalDateTime;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Dossier d'un étudiant dans le référentiel universitaire de confiance.
 *
 * Le niveau, la filière, l'UFR et le statut d'inscription proviennent de ce
 * dossier et ne sont JAMAIS lus depuis un formulaire librement renseigné par
 * l'étudiant. Le référentiel est alimenté pour l'instant par des données
 * fictives de développement ; il sera remplacé plus tard par un connecteur
 * vers le système universitaire officiel autorisé.
 */
public class ReferentielEtudiant {

    private int idReferentiel;
    private Long idExterne;
    private String numeroCarte;
    private String emailInstitutionnel;
    /** Adresse personnelle enregistrée dans le référentiel (destinataire du code). */
    private String emailPersonnel;
    private String nom;
    private String prenom;
    private String statutInscription;
    private int idFiliere;
    private int idNiveau;
    private String anneeAcademique;
    private LocalDateTime dateMiseAJour;

    /** Libellés résolus par jointure, pour l'affichage (non stockés dans le référentiel). */
    private String nomFiliere;
    private String libelleNiveau;

    public ReferentielEtudiant() {}

    public ReferentielEtudiant(int idReferentiel, String numeroCarte, String emailInstitutionnel,
                               String emailPersonnel, String nom, String prenom, String statutInscription,
                               int idFiliere, int idNiveau, String anneeAcademique) {
        this.idReferentiel = idReferentiel;
        this.numeroCarte = numeroCarte;
        this.emailInstitutionnel = emailInstitutionnel;
        this.emailPersonnel = emailPersonnel;
        this.nom = nom;
        this.prenom = prenom;
        this.statutInscription = statutInscription;
        this.idFiliere = idFiliere;
        this.idNiveau = idNiveau;
        this.anneeAcademique = anneeAcademique;
    }

    public int getIdReferentiel() { return idReferentiel; }
    public void setIdReferentiel(int idReferentiel) { this.idReferentiel = idReferentiel; }

    public Long getIdExterne() { return idExterne; }
    public void setIdExterne(Long idExterne) { this.idExterne = idExterne; }

    public String getNumeroCarte() { return numeroCarte; }
    public void setNumeroCarte(String numeroCarte) { this.numeroCarte = numeroCarte; }

    public String getEmailInstitutionnel() { return emailInstitutionnel; }
    public void setEmailInstitutionnel(String emailInstitutionnel) { this.emailInstitutionnel = emailInstitutionnel; }

    public String getEmailPersonnel() { return emailPersonnel; }
    public void setEmailPersonnel(String emailPersonnel) { this.emailPersonnel = emailPersonnel; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getStatutInscription() { return statutInscription; }
    public void setStatutInscription(String statutInscription) { this.statutInscription = statutInscription; }

    public boolean isInscriptionActive() {
        return "ACTIF".equalsIgnoreCase(statutInscription);
    }

    public int getIdFiliere() { return idFiliere; }
    public void setIdFiliere(int idFiliere) { this.idFiliere = idFiliere; }

    public int getIdNiveau() { return idNiveau; }
    public void setIdNiveau(int idNiveau) { this.idNiveau = idNiveau; }

    public String getAnneeAcademique() { return anneeAcademique; }
    public void setAnneeAcademique(String anneeAcademique) { this.anneeAcademique = anneeAcademique; }

    public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
    public void setDateMiseAJour(LocalDateTime dateMiseAJour) { this.dateMiseAJour = dateMiseAJour; }

    public String getNomFiliere() { return nomFiliere; }
    public void setNomFiliere(String nomFiliere) { this.nomFiliere = nomFiliere; }

    public String getLibelleNiveau() { return libelleNiveau; }
    public void setLibelleNiveau(String libelleNiveau) { this.libelleNiveau = libelleNiveau; }
}
// ==================== FIN CLASSE ====================
