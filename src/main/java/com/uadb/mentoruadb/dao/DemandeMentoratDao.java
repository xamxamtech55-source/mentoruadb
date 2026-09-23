package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.DemandeMentorat;
import com.uadb.mentoruadb.dto.DemandeVue;
import com.uadb.mentoruadb.dto.DemandeRecueVue;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DemandeMentoratDao implements Dao<DemandeMentorat, Integer> {

    @Override
    public DemandeMentorat create(DemandeMentorat d) throws SQLException {
        String sql = "INSERT INTO demande_mentorat (id_etudiant, id_mentor, id_matiere, date_demande, statut) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, d.getIdEtudiant());
            stmt.setInt(2, d.getIdMentor());
            stmt.setInt(3, d.getIdMatiere());
            stmt.setDate(4, Date.valueOf(d.getDateDemande()));
            stmt.setString(5, d.getStatut());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    d.setIdDemande(keys.getInt(1));
                }
            }
            return d;
        }
    }

    @Override
    public Optional<DemandeMentorat> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM demande_mentorat WHERE id_demande = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Historique des demandes envoyées par un étudiant. */
    public List<DemandeMentorat> findByEtudiant(int idEtudiant) throws SQLException {
        String sql = "SELECT * FROM demande_mentorat WHERE id_etudiant = ?";
        List<DemandeMentorat> resultats = new ArrayList<>();

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

    /** Demandes reçues par un mentor, utile pour son tableau de bord (accepter/refuser). */
    public List<DemandeMentorat> findByMentor(int idMentor) throws SQLException {
        String sql = "SELECT * FROM demande_mentorat WHERE id_mentor = ?";
        List<DemandeMentorat> resultats = new ArrayList<>();

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

    /** Demandes d'un étudiant, avec le nom du mentor et de la matière déjà résolus (JOIN). */
    public List<DemandeVue> findByEtudiantAvecDetails(int idEtudiant) throws SQLException {
        String sql = """
                SELECT d.id_demande, u.nom AS nom_mentor, u.prenom AS prenom_mentor,
                       mat.nom AS nom_matiere, d.date_demande, d.statut
                FROM demande_mentorat d
                JOIN mentor me ON d.id_mentor = me.id_mentor
                JOIN etudiant e ON me.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN matiere mat ON d.id_matiere = mat.id_matiere
                WHERE d.id_etudiant = ?
                ORDER BY d.date_demande DESC
                """;

        List<DemandeVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEtudiant);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new DemandeVue(
                            rs.getInt("id_demande"),
                            rs.getString("prenom_mentor") + " " + rs.getString("nom_mentor"),
                            rs.getString("nom_matiere"),
                            rs.getDate("date_demande").toLocalDate(),
                            rs.getString("statut")
                    ));
                }
            }
        }
        return resultats;

    }

    @Override
    public List<DemandeMentorat> findAll() throws SQLException {
        String sql = "SELECT * FROM demande_mentorat";
        List<DemandeMentorat> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resultats.add(mapRow(rs));
            }
        }
        return resultats;
    }

    /** Utilisée pour accepter/refuser une demande (changement de statut). */
    @Override
    public void update(DemandeMentorat d) throws SQLException {
        String sql = "UPDATE demande_mentorat SET id_etudiant=?, id_mentor=?, id_matiere=?, date_demande=?, statut=? WHERE id_demande=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, d.getIdEtudiant());
            stmt.setInt(2, d.getIdMentor());
            stmt.setInt(3, d.getIdMatiere());
            stmt.setDate(4, Date.valueOf(d.getDateDemande()));
            stmt.setString(5, d.getStatut());
            stmt.setInt(6, d.getIdDemande());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM demande_mentorat WHERE id_demande = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private DemandeMentorat mapRow(ResultSet rs) throws SQLException {
        return new DemandeMentorat(
                rs.getInt("id_demande"),
                rs.getInt("id_etudiant"),
                rs.getInt("id_mentor"),
                rs.getInt("id_matiere"),
                rs.getDate("date_demande").toLocalDate(),
                rs.getString("statut")
        );
    }
    /** Demandes reçues par un mentor, avec le nom de l'étudiant et de la matière résolus (JOIN). */
    public List<DemandeRecueVue> findByMentorAvecDetails(int idMentor) throws SQLException {
        String sql = """
                SELECT d.id_demande, u.nom AS nom_etudiant, u.prenom AS prenom_etudiant,
                       mat.nom AS nom_matiere, d.date_demande, d.statut
                FROM demande_mentorat d
                JOIN etudiant e ON d.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN matiere mat ON d.id_matiere = mat.id_matiere
                WHERE d.id_mentor = ?
                ORDER BY d.date_demande DESC
                """;

        List<DemandeRecueVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMentor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new DemandeRecueVue(
                            rs.getInt("id_demande"),
                            rs.getString("prenom_etudiant") + " " + rs.getString("nom_etudiant"),
                            rs.getString("nom_matiere"),
                            rs.getDate("date_demande").toLocalDate(),
                            rs.getString("statut")
                    ));
                }
            }
        }
        return resultats;
    }
    /** Étudiants ayant une demande acceptée avec ce mentor sur cette matière (pour l'invitation automatique aux séances de groupe). */
    public List<Integer> findEtudiantsAcceptesByMentorEtMatiere(int idMentor, int idMatiere) throws SQLException {
        String sql = "SELECT DISTINCT id_etudiant FROM demande_mentorat WHERE id_mentor = ? AND id_matiere = ? AND statut = 'ACCEPTEE'";
        List<Integer> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMentor);
            stmt.setInt(2, idMatiere);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(rs.getInt("id_etudiant"));
                }
            }
        }
        return resultats;
    }
}