package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.dto.CandidatMentorVue;
import com.uadb.mentoruadb.dto.MentorSuggestionVue;
import com.uadb.mentoruadb.model.Mentor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MentorDao implements Dao<Mentor, Integer> {

    @Override
    public Mentor create(Mentor m) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return create(m, conn);
        }
    }

    /** Variante transactionnelle : utilise la connexion fournie et NE la ferme PAS (c'est à l'appelant de le faire). */
    public Mentor create(Mentor m, Connection conn) throws SQLException {
        String sql = "INSERT INTO mentor (id_etudiant, statut_validation, biographie, experience, " +
                "mode_preference, nombre_max_mentores) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, m.getIdEtudiant());
            stmt.setString(2, m.getStatutValidation());
            stmt.setString(3, m.getBiographie());
            stmt.setString(4, m.getExperience());
            stmt.setString(5, m.getModePreference());
            if (m.getNombreMaxMentores() != null) {
                stmt.setInt(6, m.getNombreMaxMentores());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    m.setIdMentor(keys.getInt(1));
                }
            }
            return m;
        }
    }

    @Override
    public Optional<Mentor> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM mentor WHERE id_mentor = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Retrouver le profil mentor d'un étudiant donné (relation 1-1). */
    public Optional<Mentor> findByEtudiant(int idEtudiant) throws SQLException {
        String sql = "SELECT * FROM mentor WHERE id_etudiant = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEtudiant);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Utile pour la recherche : ne lister que les mentors déjà validés. */
    public List<Mentor> findByStatut(String statutValidation) throws SQLException {
        String sql = "SELECT * FROM mentor WHERE statut_validation = ?";
        List<Mentor> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statutValidation);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapRow(rs));
                }
            }
        }
        return resultats;
    }

    /**
     * Mentors validés suggérés pour une filière : ceux qui maîtrisent au moins une matière rattachée
     * à cette filière, triés par note moyenne décroissante (les mentors sans note passent en dernier).
     * L'étudiant qui consulte est exclu (s'il est lui-même mentor). Maximum 5 résultats.
     */
    public List<MentorSuggestionVue> findSuggestionsPourFiliere(int idFiliere, int idEtudiantExclu) throws SQLException {
        String sql = """
                SELECT me.id_mentor, u.prenom, u.nom, f.nom AS nom_filiere, n.libelle AS libelle_niveau,
                       GROUP_CONCAT(DISTINCT mat.nom ORDER BY mat.nom SEPARATOR ', ') AS matieres,
                       (SELECT ROUND(AVG(ev.note), 1)
                        FROM evaluation ev
                        JOIN seance s ON ev.id_seance = s.id_seance
                        LEFT JOIN demande_mentorat d ON s.id_demande = d.id_demande
                        WHERE COALESCE(s.id_mentor, d.id_mentor) = me.id_mentor) AS note_moyenne
                FROM mentor me
                JOIN etudiant e ON me.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN filiere f ON e.id_filiere = f.id_filiere
                JOIN niveau n ON e.id_niveau = n.id_niveau
                JOIN expertise ex ON ex.id_mentor = me.id_mentor
                JOIN matiere mat ON ex.id_matiere = mat.id_matiere
                JOIN filiere_matiere fm ON fm.id_matiere = mat.id_matiere AND fm.id_filiere = ?
                WHERE me.statut_validation = 'VALIDE' AND me.id_etudiant <> ?
                GROUP BY me.id_mentor, u.prenom, u.nom, f.nom, n.libelle
                ORDER BY note_moyenne IS NULL, note_moyenne DESC, u.nom
                LIMIT 5
                """;

        List<MentorSuggestionVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFiliere);
            stmt.setInt(2, idEtudiantExclu);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String note = rs.getString("note_moyenne");
                    resultats.add(new MentorSuggestionVue(
                            rs.getInt("id_mentor"),
                            rs.getString("prenom") + " " + rs.getString("nom"),
                            rs.getString("nom_filiere"),
                            rs.getString("libelle_niveau"),
                            rs.getString("matieres"),
                            note != null ? note + "/5" : "pas encore noté"
                    ));
                }
            }
        }
        return resultats;
    }

    /** Candidatures mentor en attente, avec étudiant/filière/niveau/matières résolus (JOIN + GROUP_CONCAT). */
    public List<CandidatMentorVue> findEnAttenteAvecDetails() throws SQLException {
        String sql = """
                SELECT me.id_mentor, u.nom, u.prenom, f.nom AS nom_filiere, n.libelle AS libelle_niveau,
                       GROUP_CONCAT(mat.nom SEPARATOR ', ') AS matieres, me.statut_validation
                FROM mentor me
                JOIN etudiant e ON me.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                JOIN filiere f ON e.id_filiere = f.id_filiere
                JOIN niveau n ON e.id_niveau = n.id_niveau
                LEFT JOIN expertise ex ON me.id_mentor = ex.id_mentor
                LEFT JOIN matiere mat ON ex.id_matiere = mat.id_matiere
                WHERE me.statut_validation = 'EN_ATTENTE'
                GROUP BY me.id_mentor, u.nom, u.prenom, f.nom, n.libelle, me.statut_validation
                ORDER BY u.nom
                """;

        List<CandidatMentorVue> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resultats.add(new CandidatMentorVue(
                        rs.getInt("id_mentor"),
                        rs.getString("prenom") + " " + rs.getString("nom"),
                        rs.getString("nom_filiere"),
                        rs.getString("libelle_niveau"),
                        rs.getString("matieres"),
                        rs.getString("statut_validation")
                ));
            }
        }
        return resultats;
    }

    /** Chemin de la photo de profil du mentor (via son compte utilisateur), pour l'affichage dans la recherche. */
    public Optional<String> findPhotoByMentor(int idMentor) throws SQLException {
        String sql = """
                SELECT u.photo
                FROM mentor m
                JOIN etudiant e ON m.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                WHERE m.id_mentor = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMentor);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("photo"));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Mentor> findAll() throws SQLException {
        String sql = "SELECT * FROM mentor";
        List<Mentor> resultats = new ArrayList<>();

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
    public void update(Mentor m) throws SQLException {
        String sql = "UPDATE mentor SET id_etudiant = ?, statut_validation = ?, biographie = ?, " +
                "experience = ?, mode_preference = ?, nombre_max_mentores = ? WHERE id_mentor = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, m.getIdEtudiant());
            stmt.setString(2, m.getStatutValidation());
            stmt.setString(3, m.getBiographie());
            stmt.setString(4, m.getExperience());
            stmt.setString(5, m.getModePreference());
            if (m.getNombreMaxMentores() != null) {
                stmt.setInt(6, m.getNombreMaxMentores());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            stmt.setInt(7, m.getIdMentor());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM mentor WHERE id_mentor = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Mentor mapRow(ResultSet rs) throws SQLException {
        int maxRaw = rs.getInt("nombre_max_mentores");
        Integer nombreMax = rs.wasNull() ? null : maxRaw;

        return new Mentor(
                rs.getInt("id_mentor"),
                rs.getInt("id_etudiant"),
                rs.getString("statut_validation"),
                rs.getString("biographie"),
                rs.getString("experience"),
                rs.getString("mode_preference"),
                nombreMax
        );
    }
}