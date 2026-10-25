package com.uadb.mentoruadb.service;

// ==================== DÉBUT IMPORTS ====================
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
public final class ReferentielMapping {

    public record Correspondance(int idFiliere, int idNiveau) {
    }

    private static final Map<String, Integer> FILIERES = Map.ofEntries(
            Map.entry("SID", 1),
            Map.entry("MAT", 2),
            Map.entry("CHI", 3),
            Map.entry("PHY", 4),
            Map.entry("SRT", 5),
            Map.entry("DAA", 6),
            Map.entry("CMM", 7),
            Map.entry("MED", 8),
            Map.entry("SCA", 9),
            Map.entry("ADD", 10),
            Map.entry("ENV", 11),
            Map.entry("ECO", 12),
            Map.entry("FIN", 13),
            Map.entry("MDO", 14),
            Map.entry("ADP", 15),
            Map.entry("JUR", 16),
            Map.entry("ECS", 17),
            Map.entry("MJE", 18),
            Map.entry("AGR", 19),
            Map.entry("ELE", 20),
            Map.entry("EAF", 21),
            Map.entry("CAR", 22),
            Map.entry("DAR", 23),
            Map.entry("GAP", 24)
    );

    private static final Map<String, Integer> NIVEAUX = Map.of(
            "L1", 1,
            "L2", 2,
            "L3", 3,
            "M1", 4,
            "M2", 5
    );

    private ReferentielMapping() {
    }

    public static Optional<Correspondance> resoudre(String codeFiliere, String codeNiveau) {
        if (codeFiliere == null || codeNiveau == null) {
            return Optional.empty();
        }
        Integer idFiliere = FILIERES.get(codeFiliere.trim().toUpperCase(Locale.ROOT));
        Integer idNiveau = NIVEAUX.get(codeNiveau.trim().toUpperCase(Locale.ROOT));
        if (idFiliere == null || idNiveau == null) {
            return Optional.empty();
        }
        return Optional.of(new Correspondance(idFiliere, idNiveau));
    }
}
// ==================== FIN CLASSE ====================
