package com.uadb.mentoruadb.dto;

// ==================== DÉBUT CLASSE ====================
public record EtudiantReferentielExterne(
        long id,
        String numeroCarte,
        String emailInstitutionnel,
        String emailPersonnel,
        String prenom,
        String nom,
        String filiereCode,
        String niveauCode,
        String statutInscription
) {
    public boolean inscriptionActive() {
        return "ACTIVE".equalsIgnoreCase(statutInscription);
    }
}
// ==================== FIN CLASSE ====================
