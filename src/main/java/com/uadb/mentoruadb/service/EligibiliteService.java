package com.uadb.mentoruadb.service;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.dao.NiveauDao;
import com.uadb.mentoruadb.dao.ReferentielEtudiantDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Niveau;
import com.uadb.mentoruadb.model.ReferentielEtudiant;

import java.sql.SQLException;
import java.util.Optional;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Éligibilité au mentorat (RG20).
 *
 * Règle prioritaire : un étudiant de Licence 1 ne peut jamais être mentor.
 * Le niveau pris en compte est celui du RÉFÉRENTIEL UNIVERSITAIRE vérifié
 * lorsqu'il existe ; le niveau déclaré dans la table etudiant n'est utilisé
 * que pour les comptes historiques créés avant la vérification. Un champ de
 * profil librement renseigné ne peut donc pas contourner la règle.
 */
public class EligibiliteService {

    /** Libellé exact du niveau interdit par RG20 (comparaison insensible à la casse). */
    public static final String NIVEAU_INELIGIBLE = "Licence 1";

    private final EtudiantDao etudiantDao = new EtudiantDao();
    private final ReferentielEtudiantDao referentielDao = new ReferentielEtudiantDao();
    private final NiveauDao niveauDao = new NiveauDao();

    /** Règle pure, testable sans base : « Licence 1 » est le seul niveau interdit. */
    public static boolean estNiveauEligible(String libelleNiveau) {
        return libelleNiveau != null
                && !libelleNiveau.isBlank()
                && !NIVEAU_INELIGIBLE.equalsIgnoreCase(libelleNiveau.trim());
    }

    /**
     * Renvoie null si l'étudiant est éligible, sinon le motif à afficher.
     * Lève SQLException uniquement en cas de panne d'accès aux données.
     */
    public String motifIneligibilite(int idEtudiant) throws SQLException {
        Optional<Etudiant> etudiantOpt = etudiantDao.findById(idEtudiant);
        if (etudiantOpt.isEmpty()) {
            return "Étudiant introuvable : l'éligibilité ne peut pas être vérifiée.";
        }
        Etudiant etudiant = etudiantOpt.get();

        int idNiveau = etudiant.getIdNiveau();
        if (etudiant.getIdReferentiel() != null) {
            Optional<ReferentielEtudiant> referentielOpt =
                    referentielDao.findById(etudiant.getIdReferentiel());
            if (referentielOpt.isPresent()) {
                ReferentielEtudiant referentiel = referentielOpt.get();
                if (!referentiel.isInscriptionActive()) {
                    return "Inscription universitaire inactive : le mentorat est indisponible.";
                }
                // Source de vérité : le niveau du référentiel, jamais celui du profil.
                idNiveau = referentiel.getIdNiveau();
            }
        }

        Optional<Niveau> niveauOpt = niveauDao.findById(idNiveau);
        if (niveauOpt.isEmpty()) {
            return "Niveau académique introuvable : l'éligibilité ne peut pas être vérifiée.";
        }
        if (!estNiveauEligible(niveauOpt.get().getLibelle())) {
            return "Éligibilité refusée (RG20) : un étudiant de " + NIVEAU_INELIGIBLE
                    + " ne peut pas être mentor.";
        }
        return null;
    }

    public boolean estEligibleMentor(int idEtudiant) throws SQLException {
        return motifIneligibilite(idEtudiant) == null;
    }

    /** Contrôle à appliquer dans les services métier (soumission et validation de candidature). */
    public void verifierEligibiliteMentor(int idEtudiant) throws SQLException {
        String motif = motifIneligibilite(idEtudiant);
        if (motif != null) {
            throw new IllegalStateException(motif);
        }
    }
}
// ==================== FIN CLASSE ====================
