package com.uadb.mentoruadb.model;

/** Entité MATIERE. */
public class Matiere {
    private int idMatiere;
    private String nom;

    public Matiere() {}

    public Matiere(int idMatiere, String nom) {
        this.idMatiere = idMatiere;
        this.nom = nom;
    }

    public int getIdMatiere() { return idMatiere; }
    public void setIdMatiere(int idMatiere) { this.idMatiere = idMatiere; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
