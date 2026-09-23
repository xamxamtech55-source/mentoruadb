package com.uadb.mentoruadb.model;

/** Entité UTILISATEUR (compte de connexion). */
public class Utilisateur {
    private int idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role;
    private String statut;
    private String telephone;
    private String photo;

    public Utilisateur() {}

    /** Constructeur historique, sans les nouveaux champs de profil (télephone/photo restent null). */
    public Utilisateur(int idUtilisateur, String nom, String prenom, String email,
                       String motDePasse, String role, String statut) {
        this(idUtilisateur, nom, prenom, email, motDePasse, role, statut, null, null);
    }

    public Utilisateur(int idUtilisateur, String nom, String prenom, String email, String motDePasse,
                       String role, String statut, String telephone, String photo) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.statut = statut;
        this.telephone = telephone;
        this.photo = photo;
    }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
}