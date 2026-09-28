package com.uadb.mentoruadb.service;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.dao.DemandeMentoratDao;
import com.uadb.mentoruadb.dao.EvaluationDao;
import com.uadb.mentoruadb.dao.SeanceDao;
import com.uadb.mentoruadb.model.DemandeMentorat;
import com.uadb.mentoruadb.model.Evaluation;
import com.uadb.mentoruadb.model.Seance;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/** Cycle de vie complet du mentorat : demande -> acceptation -> séance -> évaluation. */
public class MentoratService {

    private final DemandeMentoratDao demandeDao = new DemandeMentoratDao();
    private final SeanceDao seanceDao = new SeanceDao();
    private final EvaluationDao evaluationDao = new EvaluationDao();

    public DemandeMentorat creerDemande(int idEtudiant, int idMentor, int idMatiere) throws SQLException {
        DemandeMentorat demande = new DemandeMentorat(
                0, idEtudiant, idMentor, idMatiere, LocalDate.now(), "EN_ATTENTE"
        );
        return demandeDao.create(demande);
    }

    /**
     * Accepter une demande crée automatiquement la séance individuelle planifiée correspondante.
     * Les deux écritures (demande -> ACCEPTEE, création de la séance) sont dans UNE transaction :
     * si l'une échoue, rien n'est enregistré.
     */
    public Seance accepterDemande(int idDemande, LocalDate dateSeance, LocalTime heureDebut, LocalTime heureFin,
                                  String modalite, String lieu) throws SQLException {

        Optional<DemandeMentorat> resultat = demandeDao.findById(idDemande);
        if (resultat.isEmpty()) {
            throw new IllegalArgumentException("Demande introuvable : " + idDemande);
        }

        DemandeMentorat demande = resultat.get();
        if (!"EN_ATTENTE".equals(demande.getStatut())) {
            throw new IllegalStateException("Cette demande a déjà été traitée.");
        }
        demande.setStatut("ACCEPTEE");

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                demandeDao.update(demande, conn);
                Seance seance = Seance.individuelle(0, idDemande, dateSeance, heureDebut, heureFin, "PLANIFIEE", modalite, lieu);
                seance = seanceDao.create(seance, conn);
                conn.commit();
                return seance;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void refuserDemande(int idDemande) throws SQLException {
        Optional<DemandeMentorat> resultat = demandeDao.findById(idDemande);
        if (resultat.isEmpty()) {
            throw new IllegalArgumentException("Demande introuvable : " + idDemande);
        }

        DemandeMentorat demande = resultat.get();
        demande.setStatut("REFUSEE");
        demandeDao.update(demande);
    }

    public void marquerSeanceRealisee(int idSeance) throws SQLException {
        changerStatutSeance(idSeance, "REALISEE");
    }

    public void annulerSeance(int idSeance) throws SQLException {
        changerStatutSeance(idSeance, "ANNULEE");
    }

    private void changerStatutSeance(int idSeance, String statut) throws SQLException {
        Optional<Seance> resultat = seanceDao.findById(idSeance);
        if (resultat.isEmpty()) {
            throw new IllegalArgumentException("Séance introuvable : " + idSeance);
        }

        Seance seance = resultat.get();
        seance.setStatut(statut);
        seanceDao.update(seance);
    }

    /** Une séance ne peut être évaluée qu'une fois réalisée, et une seule fois par étudiant (contrainte UNIQUE en base). */
    public Evaluation evaluerSeance(int idSeance, int idEtudiant, int note, String commentaire) throws SQLException {
        if (note < 1 || note > 5) {
            throw new IllegalArgumentException("La note doit être comprise entre 1 et 5.");
        }

        Optional<Seance> resultatSeance = seanceDao.findById(idSeance);
        if (resultatSeance.isEmpty()) {
            throw new IllegalArgumentException("Séance introuvable : " + idSeance);
        }
        if (!"REALISEE".equals(resultatSeance.get().getStatut())) {
            throw new IllegalStateException("Seule une séance réalisée peut être évaluée.");
        }
        if (evaluationDao.findBySeanceEtEtudiant(idSeance, idEtudiant).isPresent()) {
            throw new IllegalStateException("Tu as déjà évalué cette séance.");
        }

        Evaluation evaluation = new Evaluation(0, idSeance, idEtudiant, note, commentaire, LocalDate.now());
        return evaluationDao.create(evaluation);
    }
}