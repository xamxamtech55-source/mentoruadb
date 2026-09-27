package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.dto.SeanceGroupeVue;
import com.uadb.mentoruadb.dto.SeanceMentorVue;
import com.uadb.mentoruadb.dto.SeanceVue;
import com.uadb.mentoruadb.model.Seance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeanceDao implements Dao<Seance, Integer> {

    @Override
    public Seance create(Seance s) throws SQLException {
        String sql = "INSERT INTO seance (id_demande, type_seance, id_mentor, id_matiere, date_seance, " +
                "heure_debut, heure_fin, statut, modalite, lieu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (s.getIdDemande() != null) stmt.setInt(1, s.getIdDemande()); else stmt.setNull(1, Types.INTEGER);
            stmt.setString(2, s.getTypeSeance());
            if (s.getIdMentor() != null) stmt.setInt(3, s.getIdMentor()); else stmt.setNull(3, Types.INTEGER);
            if (s.getIdMatiere() != null) stmt.setInt(4, s.getIdMatiere()); else stmt.setNull(4, Types.INTEGER);
            stmt.setDate(5, Date.valueOf(s.getDateSeance()));
            stmt.setTime(6, Time.valueOf(s.getHeureDebut()));
            stmt.setTime(7, Time.valueOf(s.getHeureFin()));
            stmt.setString(8, s.getStatut());
            stmt.setString(9, s.getModalite());
            stmt.setString(10, s.getLieu());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setIdSeance(keys.getInt(1));
                }
            }
            return s;
        }
    }

    @Override
    public Optional<Seance> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM seance WHERE id_seance = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Seance> findByDemande(int idDemande) throws SQLException {
        String sql = "SELECT * FROM seance WHERE id_demande = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idDemande);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<Seance> findGroupesByMentor(int idMentor) throws SQLException {
        String sql = "SELECT * FROM seance WHERE id_mentor = ? AND type_seance = 'GROUPE' ORDER BY date_seance DESC";
        List<Seance> resultats = new ArrayList<>();

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

    /** Séances de groupe d'un étudiant (hors celles qu'il a masquées de sa propre vue). */
    public List<SeanceVue> findGroupesByEtudiantAvecDetails(int idEtudiant) throws SQLException {
        String sql = """
                SELECT s.id_seance, u.nom AS nom_mentor, u.prenom AS prenom_mentor,
                       mat.nom AS nom_matiere, s.date_seance, s.heure_debut, s.heure_fin, s.statut,
                       s.modalite, s.lieu
                FROM seance s
                JOIN seance_participant sp ON sp.id_seance = s.id_seance
                JOIN mentor m ON s.id_mentor = m.id_mentor
                JOIN etudiant e ON m.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN matiere mat ON s.id_matiere = mat.id_matiere
                WHERE sp.id_etudiant = ? AND sp.masque = 0
                ORDER BY s.date_seance DESC
                """;

        List<SeanceVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEtudiant);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new SeanceVue(
                            rs.getInt("id_seance"),
                            rs.getString("prenom_mentor") + " " + rs.getString("nom_mentor"),
                            rs.getString("nom_matiere"),
                            rs.getDate("date_seance").toLocalDate(),
                            rs.getTime("heure_debut").toLocalTime(),
                            rs.getTime("heure_fin").toLocalTime(),
                            rs.getString("statut") + " (" + rs.getString("modalite") + " - " + rs.getString("lieu") + ")",
                            "GROUPE"
                    ));
                }
            }
        }
        return resultats;
    }

    /** Séances individuelles d'un étudiant (hors celles qu'il a masquées), avec mentor/matière résolus. */
    public List<SeanceVue> findByEtudiantAvecDetails(int idEtudiant) throws SQLException {
        String sql = """
                SELECT s.id_seance, u.nom AS nom_mentor, u.prenom AS prenom_mentor,
                       mat.nom AS nom_matiere, s.date_seance, s.heure_debut, s.heure_fin, s.statut
                FROM seance s
                JOIN demande_mentorat d ON s.id_demande = d.id_demande
                JOIN mentor me ON d.id_mentor = me.id_mentor
                JOIN etudiant e ON me.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN matiere mat ON d.id_matiere = mat.id_matiere
                WHERE d.id_etudiant = ? AND s.masque_etudiant = 0
                ORDER BY s.date_seance DESC
                """;

        List<SeanceVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEtudiant);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new SeanceVue(
                            rs.getInt("id_seance"),
                            rs.getString("prenom_mentor") + " " + rs.getString("nom_mentor"),
                            rs.getString("nom_matiere"),
                            rs.getDate("date_seance").toLocalDate(),
                            rs.getTime("heure_debut").toLocalTime(),
                            rs.getTime("heure_fin").toLocalTime(),
                            rs.getString("statut"),
                            "INDIVIDUELLE"
                    ));
                }
            }
        }
        return resultats;
    }

    /** Séances individuelles d'un mentor (hors celles qu'il a masquées), avec étudiant/matière résolus. */
    public List<SeanceMentorVue> findByMentorAvecDetails(int idMentor) throws SQLException {
        String sql = """
                SELECT s.id_seance, u.nom AS nom_etudiant, u.prenom AS prenom_etudiant,
                       mat.nom AS nom_matiere, s.date_seance, s.heure_debut, s.heure_fin, s.statut
                FROM seance s
                JOIN demande_mentorat d ON s.id_demande = d.id_demande
                JOIN etudiant e ON d.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN matiere mat ON d.id_matiere = mat.id_matiere
                WHERE d.id_mentor = ? AND s.masque_mentor = 0
                ORDER BY s.date_seance DESC
                """;

        List<SeanceMentorVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMentor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new SeanceMentorVue(
                            rs.getInt("id_seance"),
                            rs.getString("prenom_etudiant") + " " + rs.getString("nom_etudiant"),
                            rs.getString("nom_matiere"),
                            rs.getDate("date_seance").toLocalDate(),
                            rs.getTime("heure_debut").toLocalTime(),
                            rs.getTime("heure_fin").toLocalTime(),
                            rs.getString("statut")
                    ));
                }
            }
        }
        return resultats;
    }

    /** Séances de groupe non vues par l'étudiant (pour la notification cliquable). */
    public List<SeanceVue> findGroupesAVenirByEtudiant(int idEtudiant) throws SQLException {
        String sql = """
                SELECT s.id_seance, u.nom AS nom_mentor, u.prenom AS prenom_mentor,
                       mat.nom AS nom_matiere, s.date_seance, s.heure_debut, s.heure_fin, s.statut,
                       s.modalite, s.lieu
                FROM seance s
                JOIN seance_participant sp ON sp.id_seance = s.id_seance
                JOIN mentor m ON s.id_mentor = m.id_mentor
                JOIN etudiant e ON m.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN matiere mat ON s.id_matiere = mat.id_matiere
                WHERE sp.id_etudiant = ? AND sp.vu = 0 AND s.date_seance >= CURDATE() AND s.statut = 'PLANIFIEE'
                ORDER BY s.date_seance ASC
                """;

        List<SeanceVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEtudiant);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new SeanceVue(
                            rs.getInt("id_seance"),
                            rs.getString("prenom_mentor") + " " + rs.getString("nom_mentor"),
                            rs.getString("nom_matiere"),
                            rs.getDate("date_seance").toLocalDate(),
                            rs.getTime("heure_debut").toLocalTime(),
                            rs.getTime("heure_fin").toLocalTime(),
                            rs.getString("statut") + " (" + rs.getString("modalite") + " - " + rs.getString("lieu") + ")",
                            "GROUPE"
                    ));
                }
            }
        }
        return resultats;
    }

    /** Séances de groupe à venir pas encore confirmées par cet étudiant (pour le tableau "à confirmer"). */
    public List<SeanceVue> findGroupesAConfirmerByEtudiant(int idEtudiant) throws SQLException {
        String sql = """
                SELECT s.id_seance, u.nom AS nom_mentor, u.prenom AS prenom_mentor,
                       mat.nom AS nom_matiere, s.date_seance, s.heure_debut, s.heure_fin, s.statut
                FROM seance s
                JOIN seance_participant sp ON sp.id_seance = s.id_seance
                JOIN mentor m ON s.id_mentor = m.id_mentor
                JOIN etudiant e ON m.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN matiere mat ON s.id_matiere = mat.id_matiere
                WHERE sp.id_etudiant = ? AND sp.confirme = 0 AND s.statut = 'PLANIFIEE'
                ORDER BY s.date_seance ASC
                """;

        List<SeanceVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEtudiant);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new SeanceVue(
                            rs.getInt("id_seance"),
                            rs.getString("prenom_mentor") + " " + rs.getString("nom_mentor"),
                            rs.getString("nom_matiere"),
                            rs.getDate("date_seance").toLocalDate(),
                            rs.getTime("heure_debut").toLocalTime(),
                            rs.getTime("heure_fin").toLocalTime(),
                            rs.getString("statut"),
                            "GROUPE"
                    ));
                }
            }
        }
        return resultats;
    }

    /** Séances de groupe créées par un mentor (hors celles qu'il a masquées), avec matière et confirmés. */
    public List<SeanceGroupeVue> findGroupesByMentorAvecDetails(int idMentor) throws SQLException {
        String sql = """
                SELECT s.id_seance, mat.nom AS nom_matiere, s.date_seance, s.heure_debut, s.heure_fin,
                       s.statut, s.modalite, s.lieu,
                       (SELECT COUNT(*) FROM seance_participant sp WHERE sp.id_seance = s.id_seance) AS nombre_invites,
                       (SELECT COUNT(*) FROM seance_participant sp WHERE sp.id_seance = s.id_seance AND sp.confirme = 1) AS nombre_confirmes
                FROM seance s
                JOIN matiere mat ON s.id_matiere = mat.id_matiere
                WHERE s.id_mentor = ? AND s.type_seance = 'GROUPE' AND s.masque_mentor = 0
                ORDER BY s.date_seance DESC
                """;

        List<SeanceGroupeVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMentor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(new SeanceGroupeVue(
                            rs.getInt("id_seance"),
                            rs.getString("nom_matiere"),
                            rs.getDate("date_seance").toLocalDate(),
                            rs.getTime("heure_debut").toLocalTime(),
                            rs.getTime("heure_fin").toLocalTime(),
                            rs.getString("statut"),
                            rs.getString("modalite"),
                            rs.getString("lieu"),
                            rs.getInt("nombre_confirmes"),
                            rs.getInt("nombre_invites")
                    ));
                }
            }
        }
        return resultats;
    }

    public void masquerIndividuellePourEtudiant(int idSeance) throws SQLException {
        String sql = "UPDATE seance SET masque_etudiant = 1 WHERE id_seance = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idSeance);
            stmt.executeUpdate();
        }
    }

    public void masquerIndividuellePourMentor(int idSeance) throws SQLException {
        String sql = "UPDATE seance SET masque_mentor = 1 WHERE id_seance = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idSeance);
            stmt.executeUpdate();
        }
    }

    public void masquerGroupePourMentor(int idSeance) throws SQLException {
        String sql = "UPDATE seance SET masque_mentor = 1 WHERE id_seance = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idSeance);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Seance> findAll() throws SQLException {
        String sql = "SELECT * FROM seance";
        List<Seance> resultats = new ArrayList<>();

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
    public void update(Seance s) throws SQLException {
        String sql = "UPDATE seance SET statut = ?, modalite = ?, lieu = ? WHERE id_seance = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, s.getStatut());
            stmt.setString(2, s.getModalite());
            stmt.setString(3, s.getLieu());
            stmt.setInt(4, s.getIdSeance());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM seance WHERE id_seance = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Seance mapRow(ResultSet rs) throws SQLException {
        int idDemandeRaw = rs.getInt("id_demande");
        Integer idDemande = rs.wasNull() ? null : idDemandeRaw;

        int idMentorRaw = rs.getInt("id_mentor");
        Integer idMentor = rs.wasNull() ? null : idMentorRaw;

        int idMatiereRaw = rs.getInt("id_matiere");
        Integer idMatiere = rs.wasNull() ? null : idMatiereRaw;

        return new Seance(
                rs.getInt("id_seance"),
                idDemande,
                rs.getString("type_seance"),
                idMentor,
                idMatiere,
                rs.getDate("date_seance").toLocalDate(),
                rs.getTime("heure_debut").toLocalTime(),
                rs.getTime("heure_fin").toLocalTime(),
                rs.getString("statut"),
                rs.getString("modalite"),
                rs.getString("lieu")
        );
    }
}