package com.uadb.mentoruadb.model;

/** Entité NIVEAU (ex : Licence 1, Licence 2, Master 1...). */
public class Niveau {
    private int idNiveau;
    private String libelle;

    public Niveau() {}

    public Niveau(int idNiveau, String libelle) {
        this.idNiveau = idNiveau;
        this.libelle = libelle;
    }

    public int getIdNiveau() { return idNiveau; }
    public void setIdNiveau(int idNiveau) { this.idNiveau = idNiveau; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
