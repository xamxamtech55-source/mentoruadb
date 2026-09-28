package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.Filiere;
import com.uadb.mentoruadb.model.Matiere;
import com.uadb.mentoruadb.model.Niveau;

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

    public List<Filiere> findByUfr(int idUfr) throws SQLException {
        String sql = "SELECT * FROM filiere WHERE id_ufr = ? ORDER BY nom";
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

    public List<Niveau> findNiveauxByFiliere(int idFiliere) throws SQLException {
        String sql = """
                SELECT n.id_niveau, n.libelle
                FROM niveau n
                JOIN filiere_niveau fn ON n.id_niveau = fn.id_niveau
                WHERE fn.id_filiere = ?
                ORDER BY n.id_niveau
                """;

        List<Niveau> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFiliere);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new Niveau(rs.getInt("id_niveau"), rs.getString("libelle")));
                }
            }
        }
        return resultats;
    }

    /** Matières rattachées à une filière (inscription, recherche, candidature mentor, arbre admin). */
    public List<Matiere> findMatieresByFiliere(int idFiliere) throws SQLException {
        String sql = """
                SELECT m.id_matiere, m.nom
                FROM matiere m
                JOIN filiere_matiere fm ON m.id_matiere = fm.id_matiere
                WHERE fm.id_filiere = ?
                ORDER BY m.nom
                """;

        List<Matiere> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFiliere);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new Matiere(rs.getInt("id_matiere"), rs.getString("nom")));
                }
            }
        }
        return resultats;
    }

    /** Autorise un niveau pour une filière (table filiere_niveau). Sans ça, personne ne peut s'inscrire dans cette filière. */
    public void associerNiveau(int idFiliere, int idNiveau) throws SQLException {
        String sql = "INSERT IGNORE INTO filiere_niveau (id_filiere, id_niveau) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFiliere);
            stmt.setInt(2, idNiveau);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Filiere> findAll() throws SQLException {
        String sql = "SELECT * FROM filiere ORDER BY nom";
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
}