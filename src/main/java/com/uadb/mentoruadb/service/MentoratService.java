package com.uadb.mentoruadb.service;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.dao.DemandeMentoratDao;
import com.uadb.mentoruadb.dao.DemandeSeanceDao;
import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.dao.EvaluationDao;
import com.uadb.mentoruadb.dao.ExpertiseDao;
import com.uadb.mentoruadb.dao.MatiereDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dao.NotificationDao;
import com.uadb.mentoruadb.dao.SeanceDao;
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.DemandeMentorat;
import com.uadb.mentoruadb.model.DemandeSeance;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Evaluation;
import com.uadb.mentoruadb.model.Matiere;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.model.Seance;
import com.uadb.mentoruadb.model.Utilisateur;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Cycle de vie complet du mentorat, en deux étapes bien séparées :
 *
 *   1. demande_mentorat : l'étudiant demande une relation de mentorat avec un mentor sur une
 *      matière. Le mentor l'accepte ou la refuse — cette étape NE PLANIFIE AUCUNE séance.
 *
 *   2. demande_seance : une fois la relation acceptée, l'étudiant peut demander une séance quand
 *      il en a besoin (avec une date souhaitée et un message facultatifs). Le mentor l'accepte —
 *      et choisit alors la date, l'heure, la modalité et le lieu selon sa disponibilité, ce qui
 *      crée la séance — ou la refuse.
 *
 * Chaque changement de statut notifie l'autre partie (table notification).
 */
public class MentoratService {

    private final DemandeMentoratDao demandeDao = new DemandeMentoratDao();
    private final DemandeSeanceDao demandeSeanceDao = new DemandeSeanceDao();
    private final SeanceDao seanceDao = new SeanceDao();
    private final EvaluationDao evaluationDao = new EvaluationDao();
    private final ExpertiseDao expertiseDao = new ExpertiseDao();
    private final NotificationDao notificationDao = new NotificationDao();
    private final EtudiantDao etudiantDao = new EtudiantDao();
    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final MatiereDao matiereDao = new MatiereDao();
    private final MentorDao mentorDao = new MentorDao();

    // ---------------------------------------------------------------
    // Étape 1 : demande de mentorat (la relation étudiant <-> mentor <-> matière)
    // ---------------------------------------------------------------

    /**
     * Une demande ne peut porter que sur une matière que le mentor maîtrise (table expertise).
     * Ce contrôle existait déjà côté écran (RechercheMentorController) mais uniquement là : il
     * est fait ici aussi, pour qu'aucun autre chemin de création d'une demande ne puisse le
     * contourner.
     */
    public DemandeMentorat creerDemande(int idEtudiant, int idMentor, int idMatiere) throws SQLException {
        boolean maitriseLaMatiere = expertiseDao.findByMentor(idMentor).stream()
                .anyMatch(e -> e.getIdMatiere() == idMatiere);
        if (!maitriseLaMatiere) {
            throw new IllegalArgumentException("Ce mentor ne maîtrise pas cette matière.");
        }

        DemandeMentorat demande = new DemandeMentorat(0, idEtudiant, idMentor, idMatiere, LocalDate.now(), "EN_ATTENTE");
        demande = demandeDao.create(demande);

        notificationDao.creerPourMentor(idMentor,
                "Nouvelle demande de mentorat de " + nomEtudiant(idEtudiant) + " en " + nomMatiere(idMatiere) + ".");
        return demande;
    }

    /**
     * Le mentor accepte la relation de mentorat. Cela NE PLANIFIE AUCUNE séance : l'étudiant
     * devra ensuite demander une séance (voir demanderSeance) quand il en aura besoin.
     */
    public void accepterDemande(int idDemande) throws SQLException {
        DemandeMentorat demande = chargerDemande(idDemande);
        if (!"EN_ATTENTE".equals(demande.getStatut())) {
            throw new IllegalStateException("Cette demande a déjà été traitée.");
        }
        demande.setStatut("ACCEPTEE");
        demandeDao.update(demande);

        notificationDao.creerPourEtudiant(demande.getIdEtudiant(),
                nomMentor(demande.getIdMentor()) + " a accepté ta demande de mentorat en "
                        + nomMatiere(demande.getIdMatiere()) + ". Tu peux maintenant demander une séance.");
    }

    public void refuserDemande(int idDemande) throws SQLException {
        DemandeMentorat demande = chargerDemande(idDemande);
        if (!"EN_ATTENTE".equals(demande.getStatut())) {
            throw new IllegalStateException("Cette demande a déjà été traitée.");
        }
        demande.setStatut("REFUSEE");
        demandeDao.update(demande);

        notificationDao.creerPourEtudiant(demande.getIdEtudiant(),
                nomMentor(demande.getIdMentor()) + " a refusé ta demande de mentorat en "
                        + nomMatiere(demande.getIdMatiere()) + ".");
    }

    // ---------------------------------------------------------------
    // Étape 2 : demande de séance (une fois la relation acceptée)
    // ---------------------------------------------------------------

    /** L'étudiant demande une séance sur une relation de mentorat déjà acceptée. dateSouhaitee et message sont facultatifs (peuvent être null). */
    public DemandeSeance demanderSeance(int idDemande, LocalDate dateSouhaitee, String message) throws SQLException {
        DemandeMentorat demande = chargerDemande(idDemande);
        if (!"ACCEPTEE".equals(demande.getStatut())) {
            throw new IllegalStateException("Cette relation de mentorat n'est pas (encore) acceptée.");
        }

        DemandeSeance demandeSeance = new DemandeSeance(0, idDemande, dateSouhaitee, message, "EN_ATTENTE", LocalDate.now(), null);
        demandeSeance = demandeSeanceDao.create(demandeSeance);

        notificationDao.creerPourMentor(demande.getIdMentor(),
                "Nouvelle demande de séance de " + nomEtudiant(demande.getIdEtudiant())
                        + " en " + nomMatiere(demande.getIdMatiere()) + ".");
        return demandeSeance;
    }

    /**
     * Le mentor accepte une demande de séance : il choisit la date, l'heure, la modalité et le
     * lieu selon sa disponibilité, ce qui crée la séance. Les deux écritures (demande_seance ->
     * ACCEPTEE, création de la séance) sont dans UNE transaction : si l'une échoue, rien n'est
     * enregistré.
     */
    public Seance accepterDemandeSeance(int idDemandeSeance, LocalDate dateSeance, LocalTime heureDebut,
                                        LocalTime heureFin, String modalite, String lieu) throws SQLException {

        DemandeSeance demandeSeance = chargerDemandeSeance(idDemandeSeance);
        if (!"EN_ATTENTE".equals(demandeSeance.getStatut())) {
            throw new IllegalStateException("Cette demande de séance a déjà été traitée.");
        }
        DemandeMentorat demande = chargerDemande(demandeSeance.getIdDemande());
        demandeSeance.setStatut("ACCEPTEE");

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Seance seance = Seance.individuelle(0, demande.getIdDemande(), dateSeance, heureDebut, heureFin,
                        "PLANIFIEE", modalite, lieu);
                seance = seanceDao.create(seance, conn);

                demandeSeance.setIdSeance(seance.getIdSeance());
                demandeSeanceDao.update(demandeSeance, conn);

                notificationDao.creerPourEtudiant(demande.getIdEtudiant(),
                        "Ta séance de " + nomMatiere(demande.getIdMatiere()) + " avec " + nomMentor(demande.getIdMentor())
                                + " est planifiée le " + dateSeance + " de " + heureDebut + " à " + heureFin
                                + " (" + modalite + ", " + lieu + ").",
                        conn);

                conn.commit();
                return seance;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void refuserDemandeSeance(int idDemandeSeance) throws SQLException {
        DemandeSeance demandeSeance = chargerDemandeSeance(idDemandeSeance);
        if (!"EN_ATTENTE".equals(demandeSeance.getStatut())) {
            throw new IllegalStateException("Cette demande de séance a déjà été traitée.");
        }
        DemandeMentorat demande = chargerDemande(demandeSeance.getIdDemande());

        demandeSeance.setStatut("REFUSEE");
        demandeSeanceDao.update(demandeSeance);

        notificationDao.creerPourEtudiant(demande.getIdEtudiant(),
                nomMentor(demande.getIdMentor()) + " ne peut pas assurer de séance pour l'instant en "
                        + nomMatiere(demande.getIdMatiere()) + ". Tu peux refaire une demande de séance plus tard.");
    }

    // ---------------------------------------------------------------
    // Séances et évaluations (inchangé)
    // ---------------------------------------------------------------

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

    // ---------------------------------------------------------------
    // Aides internes
    // ---------------------------------------------------------------

    private DemandeMentorat chargerDemande(int idDemande) throws SQLException {
        return demandeDao.findById(idDemande)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + idDemande));
    }

    private DemandeSeance chargerDemandeSeance(int idDemandeSeance) throws SQLException {
        return demandeSeanceDao.findById(idDemandeSeance)
                .orElseThrow(() -> new IllegalArgumentException("Demande de séance introuvable : " + idDemandeSeance));
    }

    private String nomEtudiant(int idEtudiant) throws SQLException {
        Etudiant e = etudiantDao.findById(idEtudiant)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant introuvable : " + idEtudiant));
        Utilisateur u = utilisateurDao.findById(e.getIdUtilisateur())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable pour l'étudiant " + idEtudiant));
        return u.getPrenom() + " " + u.getNom();
    }

    private String nomMentor(int idMentor) throws SQLException {
        Mentor m = mentorDao.findById(idMentor)
                .orElseThrow(() -> new IllegalArgumentException("Mentor introuvable : " + idMentor));
        return nomEtudiant(m.getIdEtudiant());
    }

    private String nomMatiere(int idMatiere) throws SQLException {
        return matiereDao.findById(idMatiere).map(Matiere::getNom).orElse("une matière");
    }
}