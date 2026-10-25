package com.uadb.mentoruadb.dao;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.ReferentielEtudiant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Accès au référentiel universitaire de confiance.
 * Toutes les lectures qui décident d'une activation passent par ici : aucune
 * donnée académique n'est acceptée depuis un formulaire.
 */
public class ReferentielEtudiantDao {

    private static final String COLONNES = """
            SELECT r.id_referentiel, r.id_externe, r.numero_carte, r.email_institutionnel, r.email_personnel,
                   r.nom, r.prenom,
                   r.statut_inscription, r.id_filiere, r.id_niveau, r.annee_academique,
                   r.date_mise_a_jour, f.nom AS nom_filiere, n.libelle AS libelle_niveau
            FROM referentiel_etudiant r
            JOIN filiere f ON f.id_filiere = r.id_filiere
            JOIN niveau n ON n.id_niveau = r.id_niveau
            """;

    public Optional<ReferentielEtudiant> findByCarteEtEmail(String numeroCarte, String email) throws SQLException {
        String sql = COLONNES + " WHERE r.numero_carte = ? AND r.email_institutionnel = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroCarte);
            stmt.setString(2, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<ReferentielEtudiant> findByCarte(String numeroCarte) throws SQLException {
        String sql = COLONNES + " WHERE r.numero_carte = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroCarte);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<ReferentielEtudiant> findByEmail(String email) throws SQLException {
        String sql = COLONNES + " WHERE r.email_institutionnel = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<ReferentielEtudiant> findByIdExterne(long idExterne) throws SQLException {
        String sql = COLONNES + " WHERE r.id_externe = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idExterne);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public ReferentielEtudiant enregistrer(ReferentielEtudiant dossier) throws SQLException {
        Optional<ReferentielEtudiant> existant = Optional.empty();
        if (dossier.getIdExterne() != null) {
            existant = findByIdExterne(dossier.getIdExterne());
        }
        if (existant.isEmpty()) {
            existant = findByCarte(dossier.getNumeroCarte());
        }
        if (existant.isEmpty()) {
            existant = findByEmail(dossier.getEmailInstitutionnel());
        }
        if (existant.isPresent()) {
            dossier.setIdReferentiel(existant.get().getIdReferentiel());
            update(dossier);
            return dossier;
        }
        return create(dossier);
    }

    public Optional<ReferentielEtudiant> findById(int idReferentiel) throws SQLException {
        String sql = COLONNES + " WHERE r.id_referentiel = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idReferentiel);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Verrouille le dossier du référentiel pour la durée de la transaction :
     * deux activations simultanées du même étudiant sont sérialisées, puis la
     * seconde voit que le compte existe déjà.
     */
    public Optional<ReferentielEtudiant> findByIdPourMiseAJour(int idReferentiel, Connection conn) throws SQLException {
        String sql = COLONNES + " WHERE r.id_referentiel = ? FOR UPDATE";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReferentiel);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public ReferentielEtudiant create(ReferentielEtudiant r) throws SQLException {
        String sql = "INSERT INTO referentiel_etudiant "
                + "(id_externe, numero_carte, email_institutionnel, email_personnel, nom, prenom, "
                + "statut_inscription, id_filiere, id_niveau, annee_academique) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (r.getIdExterne() == null) {
                stmt.setNull(1, java.sql.Types.BIGINT);
            } else {
                stmt.setLong(1, r.getIdExterne());
            }
            stmt.setString(2, r.getNumeroCarte());
            stmt.setString(3, r.getEmailInstitutionnel());
            stmt.setString(4, r.getEmailPersonnel());
            stmt.setString(5, r.getNom());
            stmt.setString(6, r.getPrenom());
            stmt.setString(7, r.getStatutInscription());
            stmt.setInt(8, r.getIdFiliere());
            stmt.setInt(9, r.getIdNiveau());
            stmt.setString(10, r.getAnneeAcademique());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setIdReferentiel(keys.getInt(1));
                }
            }
            return r;
        }
    }

    public void update(ReferentielEtudiant r) throws SQLException {
        String sql = "UPDATE referentiel_etudiant SET id_externe = ?, numero_carte = ?, "
                + "email_institutionnel = ?, email_personnel = ?, nom = ?, prenom = ?, "
                + "statut_inscription = ?, id_filiere = ?, id_niveau = ?, annee_academique = ?, "
                + "date_mise_a_jour = ? WHERE id_referentiel = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (r.getIdExterne() == null) {
                stmt.setNull(1, java.sql.Types.BIGINT);
            } else {
                stmt.setLong(1, r.getIdExterne());
            }
            stmt.setString(2, r.getNumeroCarte());
            stmt.setString(3, r.getEmailInstitutionnel());
            stmt.setString(4, r.getEmailPersonnel());
            stmt.setString(5, r.getNom());
            stmt.setString(6, r.getPrenom());
            stmt.setString(7, r.getStatutInscription());
            stmt.setInt(8, r.getIdFiliere());
            stmt.setInt(9, r.getIdNiveau());
            stmt.setString(10, r.getAnneeAcademique());
            stmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(12, r.getIdReferentiel());
            stmt.executeUpdate();
        }
    }

    public List<ReferentielEtudiant> findAll() throws SQLException {
        String sql = COLONNES + " ORDER BY r.nom, r.prenom";
        List<ReferentielEtudiant> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resultats.add(mapRow(rs));
            }
        }
        return resultats;
    }

    private ReferentielEtudiant mapRow(ResultSet rs) throws SQLException {
        ReferentielEtudiant r = new ReferentielEtudiant(
                rs.getInt("id_referentiel"),
                rs.getString("numero_carte"),
                rs.getString("email_institutionnel"),
                rs.getString("email_personnel"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("statut_inscription"),
                rs.getInt("id_filiere"),
                rs.getInt("id_niveau"),
                rs.getString("annee_academique")
        );
        long idExterne = rs.getLong("id_externe");
        r.setIdExterne(rs.wasNull() ? null : idExterne);
        Timestamp maj = rs.getTimestamp("date_mise_a_jour");
        r.setDateMiseAJour(maj == null ? null : maj.toLocalDateTime());
        r.setNomFiliere(rs.getString("nom_filiere"));
        r.setLibelleNiveau(rs.getString("libelle_niveau"));
        return r;
    }
}
// ==================== FIN CLASSE ====================
