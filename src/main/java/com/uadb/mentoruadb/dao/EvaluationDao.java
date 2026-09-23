package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.Evaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EvaluationDao implements Dao<Evaluation, Integer> {

    @Override
    public Evaluation create(Evaluation e) throws SQLException {
        String sql = "INSERT INTO evaluation (id_seance, note, commentaire, date_evaluation) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, e.getIdSeance());
            stmt.setInt(2, e.getNote());
            stmt.setString(3, e.getCommentaire());
            stmt.setDate(4, Date.valueOf(e.getDateEvaluation()));
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setIdEvaluation(keys.getInt(1));
                }
            }
            return e;
        }
    }

    @Override
    public Optional<Evaluation> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM evaluation WHERE id_evaluation = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Evaluation> findBySeance(int idSeance) throws SQLException {
        String sql = "SELECT * FROM evaluation WHERE id_seance = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSeance);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Evaluation> findAll() throws SQLException {
        String sql = "SELECT * FROM evaluation";
        List<Evaluation> resultats = new ArrayList<>();

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
    public void update(Evaluation e) throws SQLException {
        String sql = "UPDATE evaluation SET id_seance=?, note=?, commentaire=?, date_evaluation=? WHERE id_evaluation=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, e.getIdSeance());
            stmt.setInt(2, e.getNote());
            stmt.setString(3, e.getCommentaire());
            stmt.setDate(4, Date.valueOf(e.getDateEvaluation()));
            stmt.setInt(5, e.getIdEvaluation());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM evaluation WHERE id_evaluation = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Evaluation mapRow(ResultSet rs) throws SQLException {
        return new Evaluation(
                rs.getInt("id_evaluation"),
                rs.getInt("id_seance"),
                rs.getInt("note"),
                rs.getString("commentaire"),
                rs.getDate("date_evaluation").toLocalDate()
        );
    }
    /** Évaluations reçues par un mentor, avec étudiant et matière résolus (JOIN). */
    public List<com.uadb.mentoruadb.dto.EvaluationVue> findByMentorAvecDetails(int idMentor) throws SQLException {
        String sql = """
                SELECT ev.id_evaluation, u.nom AS nom_etudiant, u.prenom AS prenom_etudiant,
                       mat.nom AS nom_matiere, ev.note, ev.commentaire, ev.date_evaluation
                FROM evaluation ev
                JOIN seance s ON ev.id_seance = s.id_seance
                JOIN demande_mentorat d ON s.id_demande = d.id_demande
                JOIN etudiant e ON d.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN matiere mat ON d.id_matiere = mat.id_matiere
                WHERE d.id_mentor = ?
                ORDER BY ev.date_evaluation DESC
                """;

        List<com.uadb.mentoruadb.dto.EvaluationVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMentor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new com.uadb.mentoruadb.dto.EvaluationVue(
                            rs.getInt("id_evaluation"),
                            rs.getString("prenom_etudiant") + " " + rs.getString("nom_etudiant"),
                            rs.getString("nom_matiere"),
                            rs.getInt("note"),
                            rs.getString("commentaire"),
                            rs.getDate("date_evaluation").toLocalDate()
                    ));
                }
            }
        }
        return resultats;
    }
}