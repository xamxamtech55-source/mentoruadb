package com.uadb.mentoruadb.dto;

/**
 * Un choix dans la liste "Partager avec" de l'écran mentor : soit un étudiant précis
 * (idEtudiant renseigné), soit l'option "Tous les étudiants" (idEtudiant == null).
 */
public class DestinataireVue {
    private final Integer idEtudiant;
    private final String nomAffiche;

    public DestinataireVue(Integer idEtudiant, String nomAffiche) {
        this.idEtudiant = idEtudiant;
        this.nomAffiche = nomAffiche;
    }

    public Integer getIdEtudiant() { return idEtudiant; }

    @Override
    public String toString() { return nomAffiche; }
}