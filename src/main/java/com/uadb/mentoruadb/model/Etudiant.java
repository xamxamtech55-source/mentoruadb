package com.uadb.mentoruadb.model;

// ==================== DÉBUT CLASSE ====================
/** Entité ETUDIANT (spécialisation d'un utilisateur). */
public class Etudiant {
    private int idEtudiant;
    private int idUtilisateur;
    private int idFiliere;
    private int idNiveau;
    private String numeroCarte;
    /** Dossier du référentiel universitaire vérifié ; null pour un compte historique non vérifié. */
    private Integer idReferentiel;

    public Etudiant() {}

    /** Constructeur historique, sans le numéro de carte (reste null). */
    public Etudiant(int idEtudiant, int idUtilisateur, int idFiliere, int idNiveau) {
        this(idEtudiant, idUtilisateur, idFiliere, idNiveau, null);
    }

    public Etudiant(int idEtudiant, int idUtilisateur, int idFiliere, int idNiveau, String numeroCarte) {
        this(idEtudiant, idUtilisateur, idFiliere, idNiveau, numeroCarte, null);
    }

    public Etudiant(int idEtudiant, int idUtilisateur, int idFiliere, int idNiveau,
                    String numeroCarte, Integer idReferentiel) {
        this.idEtudiant = idEtudiant;
        this.idUtilisateur = idUtilisateur;
        this.idFiliere = idFiliere;
        this.idNiveau = idNiveau;
        this.numeroCarte = numeroCarte;
        this.idReferentiel = idReferentiel;
    }

    public int getIdEtudiant() { return idEtudiant; }
    public void setIdEtudiant(int idEtudiant) { this.idEtudiant = idEtudiant; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public int getIdFiliere() { return idFiliere; }
    public void setIdFiliere(int idFiliere) { this.idFiliere = idFiliere; }

    public int getIdNiveau() { return idNiveau; }
    public void setIdNiveau(int idNiveau) { this.idNiveau = idNiveau; }

    public String getNumeroCarte() { return numeroCarte; }
    public void setNumeroCarte(String numeroCarte) { this.numeroCarte = numeroCarte; }

    public Integer getIdReferentiel() { return idReferentiel; }
    public void setIdReferentiel(Integer idReferentiel) { this.idReferentiel = idReferentiel; }
}
// ==================== FIN CLASSE ====================
