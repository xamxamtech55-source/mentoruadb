package com.uadb.mentoruadb.service;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.FiliereDao;
import com.uadb.mentoruadb.dao.NiveauDao;
import com.uadb.mentoruadb.dao.ReferentielEtudiantDao;
import com.uadb.mentoruadb.dao.ReferentielUadbDao;
import com.uadb.mentoruadb.dto.EtudiantReferentielExterne;
import com.uadb.mentoruadb.model.Filiere;
import com.uadb.mentoruadb.model.Niveau;
import com.uadb.mentoruadb.model.ReferentielEtudiant;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
public class ReferentielUniversitaireService {

    private final ReferentielUadbDao externeDao = new ReferentielUadbDao();
    private final ReferentielEtudiantDao localDao = new ReferentielEtudiantDao();
    private final FiliereDao filiereDao = new FiliereDao();
    private final NiveauDao niveauDao = new NiveauDao();

    public Optional<ReferentielEtudiant> trouverEtSynchroniser(String numeroCarte, String email)
            throws SQLException {
        String carte = numeroCarte == null ? "" : numeroCarte.trim();
        String emailNormalise = email == null ? "" : email.trim().toLowerCase();

        Optional<EtudiantReferentielExterne> externe =
                externeDao.findByCarteEtEmail(carte, emailNormalise);
        if (externe.isEmpty()) {
            return localDao.findByCarteEtEmail(carte, emailNormalise);
        }
        return Optional.of(synchroniser(externe.get()));
    }

    public ReferentielEtudiant synchroniser(EtudiantReferentielExterne externe) throws SQLException {
        ReferentielMapping.Correspondance correspondance =
                ReferentielMapping.resoudre(externe.filiereCode(), externe.niveauCode())
                        .orElseThrow(() -> new IllegalStateException(
                                "Filière ou niveau du référentiel universitaire non reconnu."));

        boolean paireAutorisee = filiereDao.findNiveauxByFiliere(correspondance.idFiliere())
                .stream()
                .anyMatch(niveau -> niveau.getIdNiveau() == correspondance.idNiveau());
        if (!paireAutorisee) {
            throw new IllegalStateException(
                    "Le niveau indiqué par le référentiel n'est pas autorisé pour cette filière.");
        }

        String nomFiliere = filiereDao.findById(correspondance.idFiliere())
                .map(Filiere::getNom).orElse(null);
        String libelleNiveau = niveauDao.findById(correspondance.idNiveau())
                .map(Niveau::getLibelle).orElse(null);

        ReferentielEtudiant dossier = new ReferentielEtudiant(
                0,
                texte(externe.numeroCarte()),
                texte(externe.emailInstitutionnel()).toLowerCase(),
                texte(externe.emailPersonnel()),
                texte(externe.nom()),
                texte(externe.prenom()),
                statutLocal(externe.statutInscription()),
                correspondance.idFiliere(),
                correspondance.idNiveau(),
                anneeAcademiqueCourante()
        );
        dossier.setIdExterne(externe.id());
        dossier.setNomFiliere(nomFiliere);
        dossier.setLibelleNiveau(libelleNiveau);

        localDao.enregistrer(dossier);
        return localDao.findByCarte(dossier.getNumeroCarte()).orElse(dossier);
    }

    public boolean estActif(String numeroCarte, Integer idReferentielLocal) throws SQLException {
        if (numeroCarte != null && !numeroCarte.isBlank()) {
            Optional<EtudiantReferentielExterne> externe =
                    externeDao.findByCarte(numeroCarte.trim());
            if (externe.isPresent()) {
                return externe.get().inscriptionActive();
            }
        }
        if (idReferentielLocal == null) {
            return true;
        }
        return localDao.findById(idReferentielLocal)
                .map(ReferentielEtudiant::isInscriptionActive)
                .orElse(false);
    }

    private static String statutLocal(String statutExterne) {
        return "ACTIVE".equalsIgnoreCase(statutExterne) ? "ACTIF" : "INACTIF";
    }

    private static String anneeAcademiqueCourante() {
        LocalDate aujourdHui = LocalDate.now();
        int debut = aujourdHui.getMonthValue() >= 9
                ? aujourdHui.getYear()
                : aujourdHui.getYear() - 1;
        return debut + "-" + (debut + 1);
    }

    private static String texte(String valeur) {
        return valeur == null ? "" : valeur.trim();
    }
}
// ==================== FIN CLASSE ====================
