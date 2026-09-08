package com.uadb.mentoruadb.model;

/** Entité FILIERE : rattachée à une UFR. */
public class Filiere {
    private int idFiliere;
    private String nom;
    private int idUfr;

    public Filiere() {}

    public Filiere(int idFiliere, String nom, int idUfr) {
        this.idFiliere = idFiliere;
        this.nom = nom;
        this.idUfr = idUfr;
    }

    public int getIdFiliere() { return idFiliere; }
    public void setIdFiliere(int idFiliere) { this.idFiliere = idFiliere; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getIdUfr() { return idUfr; }
    public void setIdUfr(int idUfr) { this.idUfr = idUfr; }
}
