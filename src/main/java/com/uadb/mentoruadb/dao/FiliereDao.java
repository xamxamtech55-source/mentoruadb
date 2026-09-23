package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.Filiere;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FiliereDao implements Dao<Filiere, Integer> {

    @Override
    public Filiere create(Filiere f) throws SQLException {
        String sql = "INSERT INTO filiere (nom, id_ufr) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, f.getNom());
            stmt.setInt(2, f.getIdUfr());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    f.setIdFiliere(keys.getInt(1));
                }
            }
            return f;
        }
    }

    @Override
    public Optional<Filiere> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM filiere WHERE id_filiere = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Utile pour la recherche de mentor : filtrer les filières d'une UFR. */
    public List<Filiere> findByUfr(int idUfr) throws SQLException {
        String sql = "SELECT * FROM filiere WHERE id_ufr = ?";
        List<Filiere> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUfr);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapRow(rs));
                }
            }
        }
        return resultats;
    }

    @Override
    public List<Filiere> findAll() throws SQLException {
        String sql = "SELECT * FROM filiere";
        List<Filiere> resultats = new ArrayList<>();

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
    public void update(Filiere f) throws SQLException {
        String sql = "UPDATE filiere SET nom = ?, id_ufr = ? WHERE id_filiere = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, f.getNom());
            stmt.setInt(2, f.getIdUfr());
            stmt.setInt(3, f.getIdFiliere());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM filiere WHERE id_filiere = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Filiere mapRow(ResultSet rs) throws SQLException {
        return new Filiere(rs.getInt("id_filiere"), rs.getString("nom"), rs.getInt("id_ufr"));
    }
    /** Niveaux valides pour une filière donnée, via la table de jointure filiere_niveau. */
    public List<com.uadb.mentoruadb.model.Niveau> findNiveauxByFiliere(int idFiliere) throws SQLException {
        String sql = """
                SELECT n.id_niveau, n.libelle
                FROM niveau n
                JOIN filiere_niveau fn ON n.id_niveau = fn.id_niveau
                WHERE fn.id_filiere = ?
                ORDER BY n.id_niveau
                """;

        List<com.uadb.mentoruadb.model.Niveau> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFiliere);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new com.uadb.mentoruadb.model.Niveau(rs.getInt("id_niveau"), rs.getString("libelle")));
                }
            }
        }
        return resultats;
    }
}