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

    /** Demandes d'un étudiant (hors celles qu'il a masquées), avec mentor/matière résolus. */
    public List<DemandeVue> findByEtudiantAvecDetails(int idEtudiant) throws SQLException {
        String sql = """
                SELECT d.id_demande, u.nom AS nom_mentor, u.prenom AS prenom_mentor,
                       mat.nom AS nom_matiere, d.date_demande, d.statut
                FROM demande_mentorat d
                JOIN mentor me ON d.id_mentor = me.id_mentor
                JOIN etudiant e ON me.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN matiere mat ON d.id_matiere = mat.id_matiere
                WHERE d.id_etudiant = ? AND d.masque_etudiant = 0
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

    /** Demandes reçues par un mentor (hors celles qu'il a masquées), avec étudiant/matière résolus. */
    public List<DemandeRecueVue> findByMentorAvecDetails(int idMentor) throws SQLException {
        String sql = """
                SELECT d.id_demande, u.nom AS nom_etudiant, u.prenom AS prenom_etudiant,
                       mat.nom AS nom_matiere, d.date_demande, d.statut
                FROM demande_mentorat d
                JOIN etudiant e ON d.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN matiere mat ON d.id_matiere = mat.id_matiere
                WHERE d.id_mentor = ? AND d.masque_mentor = 0
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
    /** Étudiants ayant une demande acceptée pour ce mentor + cette matière, avec leur nom — pour la liste "Partager avec". */
    public List<com.uadb.mentoruadb.dto.DestinataireVue> findDestinatairesAcceptesAvecNoms(int idMentor, int idMatiere) throws SQLException {
        String sql = """
                SELECT DISTINCT e.id_etudiant, u.prenom, u.nom
                FROM demande_mentorat d
                JOIN etudiant e ON e.id_etudiant = d.id_etudiant
                JOIN utilisateur u ON u.id_utilisateur = e.id_utilisateur
                WHERE d.id_mentor = ? AND d.id_matiere = ? AND d.statut = 'ACCEPTEE'
                ORDER BY u.nom
                """;
        List<com.uadb.mentoruadb.dto.DestinataireVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMentor);
            stmt.setInt(2, idMatiere);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new com.uadb.mentoruadb.dto.DestinataireVue(
                            rs.getInt("id_etudiant"), rs.getString("prenom") + " " + rs.getString("nom")));
                }
            }
        }
        return resultats;
    }


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

    /** L'étudiant masque cette demande de sa propre vue (les données restent en base, l'admin les voit toujours). */
    public void masquerPourEtudiant(int idDemande) throws SQLException {
        String sql = "UPDATE demande_mentorat SET masque_etudiant = 1 WHERE id_demande = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDemande);
            stmt.executeUpdate();
        }
    }

    /** Le mentor masque cette demande de sa propre vue. */
    public void masquerPourMentor(int idDemande) throws SQLException {
        String sql = "UPDATE demande_mentorat SET masque_mentor = 1 WHERE id_demande = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDemande);
            stmt.executeUpdate();
        }
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

    @Override
    public void update(DemandeMentorat d) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            update(d, conn);
        }
    }

    /** Variante transactionnelle : utilise la connexion fournie et NE la ferme PAS (c'est à l'appelant de le faire). */
    public void update(DemandeMentorat d, Connection conn) throws SQLException {
        String sql = "UPDATE demande_mentorat SET id_etudiant=?, id_mentor=?, id_matiere=?, date_demande=?, statut=? WHERE id_demande=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

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
}