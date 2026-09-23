package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.Expertise;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Pas de clé unique simple ici (clé composite id_mentor + id_matiere) -> pas d'interface Dao<T,ID>. */
public class ExpertiseDao {

    public void ajouter(Expertise expertise) throws SQLException {
        String sql = "INSERT INTO expertise (id_mentor, id_matiere) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, expertise.getIdMentor());
            stmt.setInt(2, expertise.getIdMatiere());
            stmt.executeUpdate();
        }
    }

    public void retirer(int idMentor, int idMatiere) throws SQLException {
        String sql = "DELETE FROM expertise WHERE id_mentor = ? AND id_matiere = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMentor);
            stmt.setInt(2, idMatiere);
            stmt.executeUpdate();
        }
    }

    /** Toutes les matières dans lesquelles un mentor est expert. */
    public List<Expertise> findByMentor(int idMentor) throws SQLException {
        String sql = "SELECT * FROM expertise WHERE id_mentor = ?";
        List<Expertise> resultats = new ArrayList<>();

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

    /** Tous les mentors experts dans une matière donnée — cœur de la recherche de mentor. */
    public List<Expertise> findByMatiere(int idMatiere) throws SQLException {
        String sql = "SELECT * FROM expertise WHERE id_matiere = ?";
        List<Expertise> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMatiere);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapRow(rs));
                }
            }
        }
        return resultats;
    }

    private Expertise mapRow(ResultSet rs) throws SQLException {
        return new Expertise(rs.getInt("id_mentor"), rs.getInt("id_matiere"));
    }
    /** Mentors validés qui maîtrisent une matière donnée — cœur de la recherche de mentor. */
    public java.util.List<com.uadb.mentoruadb.dto.MentorVue> findMentorsValidesParMatiere(int idMatiere) throws SQLException {
        String sql = """
                SELECT me.id_mentor, u.nom AS nom, u.prenom AS prenom,
                       f.nom AS nom_filiere, n.libelle AS libelle_niveau
                FROM expertise ex
                JOIN mentor me ON ex.id_mentor = me.id_mentor
                JOIN etudiant e ON me.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN filiere f ON e.id_filiere = f.id_filiere
                JOIN niveau n ON e.id_niveau = n.id_niveau
                WHERE ex.id_matiere = ? AND me.statut_validation = 'VALIDE'
                ORDER BY u.nom
                """;

        java.util.List<com.uadb.mentoruadb.dto.MentorVue> resultats = new java.util.ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMatiere);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new com.uadb.mentoruadb.dto.MentorVue(
                            rs.getInt("id_mentor"),
                            rs.getString("prenom") + " " + rs.getString("nom"),
                            rs.getString("nom_filiere"),
                            rs.getString("libelle_niveau")
                    ));
                }
            }
        }
        return resultats;
    }
    /** Matières dans lesquelles un mentor est expert, sous forme d'objets Matiere exploitables en ComboBox. */
    public java.util.List<com.uadb.mentoruadb.model.Matiere> findMatieresByMentor(int idMentor) throws SQLException {
        String sql = """
                SELECT mat.id_matiere, mat.nom
                FROM expertise ex
                JOIN matiere mat ON ex.id_matiere = mat.id_matiere
                WHERE ex.id_mentor = ?
                ORDER BY mat.nom
                """;

        java.util.List<com.uadb.mentoruadb.model.Matiere> resultats = new java.util.ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMentor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new com.uadb.mentoruadb.model.Matiere(rs.getInt("id_matiere"), rs.getString("nom")));
                }
            }
        }
        return resultats;
    }
}