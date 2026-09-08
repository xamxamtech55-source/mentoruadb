package com.uadb.mentoruadb.model;

/** Entité UFR. */
public class Ufr {
    private int idUfr;
    private String nom;

    public Ufr() {}

    public Ufr(int idUfr, String nom) {
        this.idUfr = idUfr;
        this.nom = nom;
    }

    public int getIdUfr() { return idUfr; }
    public void setIdUfr(int idUfr) { this.idUfr = idUfr; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
