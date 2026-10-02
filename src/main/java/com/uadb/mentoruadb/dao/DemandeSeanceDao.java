package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.dto.DemandeSeanceRecueVue;
import com.uadb.mentoruadb.dto.DemandeSeanceVue;
import com.uadb.mentoruadb.model.DemandeSeance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DemandeSeanceDao implements Dao<DemandeSeance, Integer> {

    @Override
    public DemandeSeance create(DemandeSeance d) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return create(d, conn);
        }
    }

    /** Variante transactionnelle : utilise la connexion fournie et NE la ferme PAS (c'est à l'appelant de le faire). */
    public DemandeSeance create(DemandeSeance d, Connection conn) throws SQLException {
        String sql = "INSERT INTO demande_seance (id_demande, date_souhaitee, message, statut, date_creation, id_seance) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, d.getIdDemande());
            if (d.getDateSouhaitee() != null) stmt.setDate(2, Date.valueOf(d.getDateSouhaitee())); else stmt.setNull(2, Types.DATE);
            stmt.setString(3, d.getMessage());
            stmt.setString(4, d.getStatut());
            stmt.setDate(5, Date.valueOf(d.getDateCreation()));
            if (d.getIdSeance() != null) stmt.setInt(6, d.getIdSeance()); else stmt.setNull(6, Types.INTEGER);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    d.setIdDemandeSeance(keys.getInt(1));
                }
            }
            return d;
        }
    }

    @Override
    public Optional<DemandeSeance> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM demande_seance WHERE id_demande_seance = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Demandes de séance d'un étudiant (hors masquées), avec mentor/matière/statut résolus, et la séance planifiée si acceptée. */
    public List<DemandeSeanceVue> findByEtudiantAvecDetails(int idEtudiant) throws SQLException {
        String sql = """
                SELECT ds.id_demande_seance, u.nom AS nom_mentor, u.prenom AS prenom_mentor,
                       mat.nom AS nom_matiere, ds.date_souhaitee, ds.message, ds.statut,
                       s.date_seance, s.heure_debut, s.heure_fin
                FROM demande_seance ds
                JOIN demande_mentorat d ON d.id_demande = ds.id_demande
                JOIN mentor me ON me.id_mentor = d.id_mentor
                JOIN etudiant em ON em.id_etudiant = me.id_etudiant
                JOIN utilisateur u ON u.id_utilisateur = em.id_utilisateur
                JOIN matiere mat ON mat.id_matiere = d.id_matiere
                LEFT JOIN seance s ON s.id_seance = ds.id_seance
                WHERE d.id_etudiant = ? AND ds.masque_etudiant = 0
                ORDER BY ds.date_creation DESC
                """;
        List<DemandeSeanceVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEtudiant);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date dateSouhaiteeRaw = rs.getDate("date_souhaitee");
                    Date dateSeanceRaw = rs.getDate("date_seance");
                    Time heureDebutRaw = rs.getTime("heure_debut");
                    Time heureFinRaw = rs.getTime("heure_fin");
                    resultats.add(new DemandeSeanceVue(
                            rs.getInt("id_demande_seance"),
                            rs.getString("prenom_mentor") + " " + rs.getString("nom_mentor"),
                            rs.getString("nom_matiere"),
                            dateSouhaiteeRaw == null ? null : dateSouhaiteeRaw.toLocalDate(),
                            rs.getString("message"),
                            rs.getString("statut"),
                            dateSeanceRaw == null ? null : dateSeanceRaw.toLocalDate(),
                            heureDebutRaw == null ? null : heureDebutRaw.toLocalTime(),
                            heureFinRaw == null ? null : heureFinRaw.toLocalTime()
                    ));
                }
            }
        }
        return resultats;
    }

    /** Demandes de séance reçues par un mentor (hors masquées), avec étudiant/matière/statut résolus. */
    public List<DemandeSeanceRecueVue> findByMentorAvecDetails(int idMentor) throws SQLException {
        String sql = """
                SELECT ds.id_demande_seance, u.nom AS nom_etudiant, u.prenom AS prenom_etudiant,
                       mat.nom AS nom_matiere, ds.date_souhaitee, ds.message, ds.statut, ds.date_creation
                FROM demande_seance ds
                JOIN demande_mentorat d ON d.id_demande = ds.id_demande
                JOIN etudiant e ON e.id_etudiant = d.id_etudiant
                JOIN utilisateur u ON u.id_utilisateur = e.id_utilisateur
                JOIN matiere mat ON mat.id_matiere = d.id_matiere
                WHERE d.id_mentor = ? AND ds.masque_mentor = 0
                ORDER BY ds.date_creation DESC
                """;
        List<DemandeSeanceRecueVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMentor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date dateSouhaiteeRaw = rs.getDate("date_souhaitee");
                    resultats.add(new DemandeSeanceRecueVue(
                            rs.getInt("id_demande_seance"),
                            rs.getString("prenom_etudiant") + " " + rs.getString("nom_etudiant"),
                            rs.getString("nom_matiere"),
                            dateSouhaiteeRaw == null ? null : dateSouhaiteeRaw.toLocalDate(),
                            rs.getString("message"),
                            rs.getString("statut"),
                            rs.getDate("date_creation").toLocalDate()
                    ));
                }
            }
        }
        return resultats;
    }

    /** L'étudiant masque cette demande de séance de sa propre vue. */
    public void masquerPourEtudiant(int idDemandeSeance) throws SQLException {
        String sql = "UPDATE demande_seance SET masque_etudiant = 1 WHERE id_demande_seance = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDemandeSeance);
            stmt.executeUpdate();
        }
    }

    /** Le mentor masque cette demande de séance de sa propre vue. */
    public void masquerPourMentor(int idDemandeSeance) throws SQLException {
        String sql = "UPDATE demande_seance SET masque_mentor = 1 WHERE id_demande_seance = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDemandeSeance);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<DemandeSeance> findAll() throws SQLException {
        String sql = "SELECT * FROM demande_seance";
        List<DemandeSeance> resultats = new ArrayList<>();

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
    public void update(DemandeSeance d) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            update(d, conn);
        }
    }

    /** Variante transactionnelle : utilise la connexion fournie et NE la ferme PAS (c'est à l'appelant de le faire). */
    public void update(DemandeSeance d, Connection conn) throws SQLException {
        String sql = "UPDATE demande_seance SET id_demande=?, date_souhaitee=?, message=?, statut=?, " +
                "date_creation=?, id_seance=? WHERE id_demande_seance=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, d.getIdDemande());
            if (d.getDateSouhaitee() != null) stmt.setDate(2, Date.valueOf(d.getDateSouhaitee())); else stmt.setNull(2, Types.DATE);
            stmt.setString(3, d.getMessage());
            stmt.setString(4, d.getStatut());
            stmt.setDate(5, Date.valueOf(d.getDateCreation()));
            if (d.getIdSeance() != null) stmt.setInt(6, d.getIdSeance()); else stmt.setNull(6, Types.INTEGER);
            stmt.setInt(7, d.getIdDemandeSeance());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM demande_seance WHERE id_demande_seance = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private DemandeSeance mapRow(ResultSet rs) throws SQLException {
        int idSeanceRaw = rs.getInt("id_seance");
        Integer idSeance = rs.wasNull() ? null : idSeanceRaw;
        Date dateSouhaiteeRaw = rs.getDate("date_souhaitee");

        return new DemandeSeance(
                rs.getInt("id_demande_seance"),
                rs.getInt("id_demande"),
                dateSouhaiteeRaw == null ? null : dateSouhaiteeRaw.toLocalDate(),
                rs.getString("message"),
                rs.getString("statut"),
                rs.getDate("date_creation").toLocalDate(),
                idSeance
        );
    }
}