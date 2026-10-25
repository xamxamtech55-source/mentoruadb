package com.uadb.mentoruadb.service;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.dao.ExpertiseDao;
import com.uadb.mentoruadb.dao.FiliereDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Expertise;
import com.uadb.mentoruadb.model.Matiere;
import com.uadb.mentoruadb.model.Mentor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Soumission d'une candidature « devenir mentor ».
 *
 * Les contrôles sont ici, et non dans le contrôleur JavaFX : masquer un bouton
 * ne protège rien. RG20 est appliqué à partir du référentiel universitaire
 * vérifié (voir EligibiliteService), avant toute écriture.
 */
public class CandidatureMentorService {

    private final EtudiantDao etudiantDao = new EtudiantDao();
    private final MentorDao mentorDao = new MentorDao();
    private final ExpertiseDao expertiseDao = new ExpertiseDao();
    private final FiliereDao filiereDao = new FiliereDao();
    private final EligibiliteService eligibiliteService = new EligibiliteService();

    public Mentor soumettreCandidature(int idEtudiant, List<Integer> idMatieres) throws SQLException {
        Etudiant etudiant = etudiantDao.findById(idEtudiant)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant introuvable."));

        eligibiliteService.verifierEligibiliteMentor(idEtudiant);

        if (idMatieres == null || idMatieres.isEmpty()) {
            throw new IllegalArgumentException("Choisis au moins une matière.");
        }
        if (mentorDao.findByEtudiant(idEtudiant).isPresent()) {
            throw new IllegalStateException("Tu as déjà une candidature mentor.");
        }

        // Les matières proposées doivent réellement appartenir à la filière de l'étudiant.
        Set<Integer> matieresDeLaFiliere = new HashSet<>();
        for (Matiere matiere : filiereDao.findMatieresByFiliere(etudiant.getIdFiliere())) {
            matieresDeLaFiliere.add(matiere.getIdMatiere());
        }
        for (Integer idMatiere : idMatieres) {
            if (idMatiere == null || !matieresDeLaFiliere.contains(idMatiere)) {
                throw new IllegalArgumentException("Une des matières choisies n'appartient pas à ta filière.");
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Mentor mentor = mentorDao.create(new Mentor(0, idEtudiant, "EN_ATTENTE"), conn);
                for (Integer idMatiere : new HashSet<>(idMatieres)) {
                    expertiseDao.ajouter(new Expertise(mentor.getIdMentor(), idMatiere), conn);
                }
                conn.commit();
                return mentor;
            } catch (SQLIntegrityConstraintViolationException e) {
                conn.rollback();
                throw new IllegalStateException("Une candidature mentor existe déjà pour ce compte.");
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
// ==================== FIN CLASSE ====================
