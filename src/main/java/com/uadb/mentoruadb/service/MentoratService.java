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
import com.uadb.mentoruadb.dao.SeanceParticipantDao;
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
import java.util.List;
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
    private final SeanceParticipantDao seanceParticipantDao = new SeanceParticipantDao();
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
     * Crée une demande de mentorat. Contrôles métier (indépendants de l'écran, pour qu'aucun
     * autre chemin ne les contourne) :
     *   - le mentor doit exister et être VALIDE (RG : jamais de mentor non validé) ;
     *   - un étudiant ne peut pas se demander lui-même comme mentor ;
     *   - la matière demandée doit figurer dans les expertises du mentor (RG23) ;
     *   - une seule demande EN_ATTENTE à la fois pour un même couple mentor + matière
     *     (empêche la double demande, y compris en cas de double clic).
     */
    public DemandeMentorat creerDemande(int idEtudiant, int idMentor, int idMatiere) throws SQLException {
        Mentor mentor = mentorDao.findById(idMentor)
                .orElseThrow(() -> new IllegalArgumentException("Mentor introuvable."));
        if (!"VALIDE".equals(mentor.getStatutValidation())) {
            throw new IllegalArgumentException("Ce mentor n'est pas encore validé.");
        }
        if (mentor.getIdEtudiant() == idEtudiant) {
            throw new IllegalArgumentException("Tu ne peux pas demander un mentorat à toi-même.");
        }

        boolean maitriseLaMatiere = expertiseDao.findByMentor(idMentor).stream()
                .anyMatch(e -> e.getIdMatiere() == idMatiere);
        if (!maitriseLaMatiere) {
            throw new IllegalArgumentException("Ce mentor ne maîtrise pas cette matière.");
        }

        boolean dejaEnAttente = demandeDao.findByEtudiant(idEtudiant).stream()
                .anyMatch(d -> d.getIdMentor() == idMentor && d.getIdMatiere() == idMatiere
                        && "EN_ATTENTE".equals(d.getStatut()));
        if (dejaEnAttente) {
            throw new IllegalStateException("Une demande est déjà en attente pour ce mentor et cette matière.");
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
    // Séances de groupe
    // ---------------------------------------------------------------

    /**
     * Crée une séance de groupe et invite, dans UNE transaction, tous les étudiants ayant une
     * demande de mentorat ACCEPTEE avec ce mentor sur cette matière (aucun participant si ce
     * list est vide — une séance de groupe sans participant n'a pas de sens). Chaque étudiant
     * invité est notifié (RG27). Contrôles : mentor VALIDE, matière dans ses expertises (RG23),
     * date non passée, heure_fin &gt; heure_debut (RG11).
     */
    public Seance creerSeanceDeGroupe(int idMentor, int idMatiere, LocalDate dateSeance, LocalTime heureDebut,
                                      LocalTime heureFin, String modalite, String lieu) throws SQLException {

        Mentor mentor = mentorDao.findById(idMentor)
                .orElseThrow(() -> new IllegalArgumentException("Mentor introuvable."));
        if (!"VALIDE".equals(mentor.getStatutValidation())) {
            throw new IllegalArgumentException("Seul un mentor validé peut créer une séance de groupe.");
        }
        if (dateSeance == null) {
            throw new IllegalArgumentException("La date de la séance est obligatoire.");
        }
        if (dateSeance.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date de la séance ne peut pas être dans le passé.");
        }
        if (heureDebut == null || heureFin == null || !heureFin.isAfter(heureDebut)) {
            throw new IllegalArgumentException("L'heure de fin doit être après l'heure de début.");
        }
        boolean maitriseLaMatiere = expertiseDao.findByMentor(idMentor).stream()
                .anyMatch(e -> e.getIdMatiere() == idMatiere);
        if (!maitriseLaMatiere) {
            throw new IllegalArgumentException("Ce mentor ne maîtrise pas cette matière.");
        }

        List<Integer> etudiants = demandeDao.findEtudiantsAcceptesByMentorEtMatiere(idMentor, idMatiere);
        if (etudiants.isEmpty()) {
            throw new IllegalStateException("Aucun étudiant n'a une demande acceptée en "
                    + nomMatiere(idMatiere) + " — cette séance de groupe n'aurait aucun participant.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Seance seance = Seance.groupe(0, idMentor, idMatiere, dateSeance, heureDebut, heureFin,
                        "PLANIFIEE", modalite, lieu);
                seance = seanceDao.create(seance, conn);

                for (Integer idEtudiant : etudiants) {
                    seanceParticipantDao.ajouter(seance.getIdSeance(), idEtudiant, conn);
                    notificationDao.creerPourEtudiant(idEtudiant,
                            "Nouvelle séance de groupe en " + nomMatiere(idMatiere) + " avec " + nomMentor(idMentor)
                                    + " le " + dateSeance + " de " + heureDebut + " à " + heureFin
                                    + " (" + modalite + ", " + lieu + "). Confirme ta présence depuis ton tableau de bord.",
                            conn);
                }

                conn.commit();
                return seance;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
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
        if (!"PLANIFIEE".equals(seance.getStatut())) {
            throw new IllegalStateException(
                    "Seule une séance planifiée peut être modifiée (statut actuel : " + seance.getStatut() + ").");
        }
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