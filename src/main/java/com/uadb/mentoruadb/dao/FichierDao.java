package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.Fichier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FichierDao implements Dao<Fichier, Integer> {

    @Override
    public Fichier create(Fichier f) throws SQLException {
        String sql = "INSERT INTO fichier (id_mentor, id_matiere, id_seance, nom_fichier, chemin, date_upload) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, f.getIdMentor());
            if (f.getIdMatiere() != null) stmt.setInt(2, f.getIdMatiere()); else stmt.setNull(2, Types.INTEGER);
            if (f.getIdSeance() != null) stmt.setInt(3, f.getIdSeance()); else stmt.setNull(3, Types.INTEGER);
            stmt.setString(4, f.getNomFichier());
            stmt.setString(5, f.getChemin());
            stmt.setDate(6, Date.valueOf(f.getDateUpload()));
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    f.setIdFichier(keys.getInt(1));
                }
            }
            return f;
        }
    }

    @Override
    public Optional<Fichier> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM fichier WHERE id_fichier = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Tous les fichiers partagés par un mentor (sa liste personnelle de gestion). */
    public List<Fichier> findByMentor(int idMentor) throws SQLException {
        String sql = "SELECT * FROM fichier WHERE id_mentor = ? ORDER BY date_upload DESC";
        List<Fichier> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMentor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapRow(rs));
                }
            }
        }
        return resultats;
    }

    /** Fichiers visibles par un étudiant : ceux liés aux matières pour lesquelles il a une demande acceptée. */
    public List<Fichier> findVisiblesParEtudiant(int idEtudiant) throws SQLException {
        String sql = """
                SELECT DISTINCT f.*
                FROM fichier f
                JOIN demande_mentorat d ON d.id_mentor = f.id_mentor AND d.id_matiere = f.id_matiere
                WHERE d.id_etudiant = ? AND d.statut = 'ACCEPTEE'
                ORDER BY f.date_upload DESC
                """;
        List<Fichier> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEtudiant);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapRow(rs));
                }
            }
        }
        return resultats;
    }

    @Override
    public List<Fichier> findAll() throws SQLException {
        String sql = "SELECT * FROM fichier";
        List<Fichier> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resultats.add(mapRow(rs));
            }
        }
        return resultats;
    }

    @Override
    public void update(Fichier f) throws SQLException {
        String sql = "UPDATE fichier SET nom_fichier = ?, chemin = ? WHERE id_fichier = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, f.getNomFichier());
            stmt.setString(2, f.getChemin());
            stmt.setInt(3, f.getIdFichier());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM fichier WHERE id_fichier = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Fichier mapRow(ResultSet rs) throws SQLException {
        int idMatiereRaw = rs.getInt("id_matiere");
        Integer idMatiere = rs.wasNull() ? null : idMatiereRaw;

        int idSeanceRaw = rs.getInt("id_seance");
        Integer idSeance = rs.wasNull() ? null : idSeanceRaw;

        return new Fichier(
                rs.getInt("id_fichier"),
                rs.getInt("id_mentor"),
                idMatiere,
                idSeance,
                rs.getString("nom_fichier"),
                rs.getString("chemin"),
                rs.getDate("date_upload").toLocalDate()
        );
    }
}