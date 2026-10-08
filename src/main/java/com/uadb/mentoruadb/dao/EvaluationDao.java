package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.dto.EvaluationEtudiantVue;
import com.uadb.mentoruadb.dto.EvaluationVue;
import com.uadb.mentoruadb.model.Evaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EvaluationDao implements Dao<Evaluation, Integer> {

    @Override
    public Evaluation create(Evaluation e) throws SQLException {
        String sql = "INSERT INTO evaluation (id_seance, id_etudiant, note, commentaire, date_evaluation) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, e.getIdSeance());
            stmt.setInt(2, e.getIdEtudiant());
            stmt.setInt(3, e.getNote());
            stmt.setString(4, e.getCommentaire());
            stmt.setDate(5, Date.valueOf(e.getDateEvaluation()));
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

    /** Une évaluation précise faite par un étudiant précis pour une séance (empêche le doublon). */
    public Optional<Evaluation> findBySeanceEtEtudiant(int idSeance, int idEtudiant) throws SQLException {
        String sql = "SELECT * FROM evaluation WHERE id_seance = ? AND id_etudiant = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSeance);
            stmt.setInt(2, idEtudiant);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Évaluations reçues par un mentor, individuelles ET de groupe (COALESCE sur les deux chemins possibles). */
    public List<EvaluationVue> findByMentorAvecDetails(int idMentor) throws SQLException {
        String sql = """
                SELECT ev.id_evaluation, u.nom AS nom_etudiant, u.prenom AS prenom_etudiant,
                       ev.note, ev.commentaire, ev.date_evaluation,
                       COALESCE(matg.nom, matd.nom) AS nom_matiere
                FROM evaluation ev
                JOIN seance s ON ev.id_seance = s.id_seance
                JOIN etudiant e ON ev.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                LEFT JOIN demande_mentorat d ON s.id_demande = d.id_demande
                LEFT JOIN matiere matd ON d.id_matiere = matd.id_matiere
                LEFT JOIN matiere matg ON s.id_matiere = matg.id_matiere
                WHERE COALESCE(s.id_mentor, d.id_mentor) = ? AND ev.masque_mentor = 0
                ORDER BY ev.date_evaluation DESC
                """;

        List<EvaluationVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMentor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new EvaluationVue(
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

    /** Évaluations données par un étudiant, individuelles ET de groupe (COALESCE sur les deux chemins possibles). */
    public List<EvaluationEtudiantVue> findByEtudiantAvecDetails(int idEtudiant) throws SQLException {
        String sql = """
                SELECT ev.id_evaluation, u.nom AS nom_mentor, u.prenom AS prenom_mentor,
                       ev.note, ev.commentaire, ev.date_evaluation,
                       COALESCE(matg.nom, matd.nom) AS nom_matiere
                FROM evaluation ev
                JOIN seance s ON ev.id_seance = s.id_seance
                LEFT JOIN demande_mentorat d ON s.id_demande = d.id_demande
                LEFT JOIN matiere matd ON d.id_matiere = matd.id_matiere
                LEFT JOIN matiere matg ON s.id_matiere = matg.id_matiere
                JOIN mentor m ON COALESCE(s.id_mentor, d.id_mentor) = m.id_mentor
                JOIN etudiant e ON m.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                WHERE ev.id_etudiant = ?
                ORDER BY ev.date_evaluation DESC
                """;

        List<EvaluationEtudiantVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEtudiant);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new EvaluationEtudiantVue(
                            rs.getInt("id_evaluation"),
                            rs.getString("prenom_mentor") + " " + rs.getString("nom_mentor"),
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
        String sql = "UPDATE evaluation SET id_seance=?, id_etudiant=?, note=?, commentaire=?, date_evaluation=? WHERE id_evaluation=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, e.getIdSeance());
            stmt.setInt(2, e.getIdEtudiant());
            stmt.setInt(3, e.getNote());
            stmt.setString(4, e.getCommentaire());
            stmt.setDate(5, Date.valueOf(e.getDateEvaluation()));
            stmt.setInt(6, e.getIdEvaluation());
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
                rs.getInt("id_etudiant"),
                rs.getInt("note"),
                rs.getString("commentaire"),
                rs.getDate("date_evaluation").toLocalDate()
        );
    }
    /** Le mentor masque une évaluation reçue de sa propre vue. */
    public void masquerPourMentor(int idEvaluation) throws SQLException {
        String sql = "UPDATE evaluation SET masque_mentor = 1 WHERE id_evaluation = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEvaluation);
            stmt.executeUpdate();
        }
    }
}