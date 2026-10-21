package com.uadb.mentoruadb.service;

import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dao.NiveauDao;
import com.uadb.mentoruadb.dao.NotificationDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.model.Niveau;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Décisions d'administration sur les candidatures « devenir mentor ».
 *
 * Les règles sont appliquées ici (et non dans l'écran) pour qu'aucun chemin ne les contourne :
 *   - une candidature déjà traitée ne peut plus changer de statut ;
 *   - l'éligibilité est vérifiée avant la validation (RG20 : un étudiant de Licence 1
 *     ne peut pas être mentor) ;
 *   - chaque décision notifie le candidat (RG27).
 *
 * Il n'existe pas de champ « motif de refus » dans le schéma : le refus est enregistré
 * par le seul statut REFUSE, la notification portant l'information au candidat.
 */
public class AdminService {

    private final MentorDao mentorDao = new MentorDao();
    private final EtudiantDao etudiantDao = new EtudiantDao();
    private final NiveauDao niveauDao = new NiveauDao();
    private final NotificationDao notificationDao = new NotificationDao();

    private static final String NIVEAU_INELIGIBLE = "Licence 1";

    /** Valide une candidature EN_ATTENTE, après contrôle d'éligibilité (RG20). */
    public Mentor validerCandidature(int idMentor) throws SQLException {
        Mentor mentor = chargerCandidature(idMentor);
        Etudiant etudiant = chargerEtudiant(mentor);
        verifierEligibilite(etudiant);

        mentor.setStatutValidation("VALIDE");
        mentorDao.update(mentor);

        notificationDao.creerPourEtudiant(etudiant.getIdEtudiant(),
                "Ta candidature mentor a été validée par l'administration. Tu peux maintenant recevoir des demandes de mentorat.");
        return mentor;
    }

    /** Refuse une candidature EN_ATTENTE. Le statut REFUSE est conservé en base (pas de champ motif dans le schéma). */
    public Mentor refuserCandidature(int idMentor) throws SQLException {
        Mentor mentor = chargerCandidature(idMentor);
        Etudiant etudiant = chargerEtudiant(mentor);

        mentor.setStatutValidation("REFUSE");
        mentorDao.update(mentor);

        notificationDao.creerPourEtudiant(etudiant.getIdEtudiant(),
                "Ta candidature mentor a été refusée par l'administration. Tu pourras refaire une candidature plus tard.");
        return mentor;
    }

    private Mentor chargerCandidature(int idMentor) throws SQLException {
        Mentor mentor = mentorDao.findById(idMentor)
                .orElseThrow(() -> new IllegalArgumentException("Candidature introuvable."));
        if (!"EN_ATTENTE".equals(mentor.getStatutValidation())) {
            throw new IllegalStateException(
                    "Cette candidature a déjà été traitée (statut : " + mentor.getStatutValidation() + ").");
        }
        return mentor;
    }

    private Etudiant chargerEtudiant(Mentor mentor) throws SQLException {
        return etudiantDao.findById(mentor.getIdEtudiant())
                .orElseThrow(() -> new IllegalArgumentException("Étudiant introuvable pour cette candidature."));
    }

    /** RG20 : Licence 1 ne peut pas être mentor. Appelé uniquement pour une validation. */
    private void verifierEligibilite(Etudiant etudiant) throws SQLException {
        Optional<Niveau> niveau = niveauDao.findById(etudiant.getIdNiveau());
        if (niveau.isPresent() && NIVEAU_INELIGIBLE.equalsIgnoreCase(niveau.get().getLibelle())) {
            throw new IllegalStateException(
                    "Éligibilité refusée (RG20) : un étudiant de " + NIVEAU_INELIGIBLE + " ne peut pas être mentor.");
        }
    }
}
