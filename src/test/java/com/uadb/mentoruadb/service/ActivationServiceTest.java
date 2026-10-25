package com.uadb.mentoruadb.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests sans base : validation des entrées et des tickets avant tout accès JDBC.
 */
class ActivationServiceTest {

    private final ActivationService service = new ActivationService((destinataire, sujet, corps) -> { }, false);

    @Test
    void numeroDeCarteInvalideRefuse() {
        assertThrows(IllegalArgumentException.class, () ->
                service.demanderCode("123", "etudiant@uadb.edu.sn", "perso@gmail.com"));
        assertThrows(IllegalArgumentException.class, () ->
                service.demanderCode("12345678", "etudiant@uadb.edu.sn", "perso@gmail.com"));
    }

    @Test
    void emailNonInstitutionnelRefuse() {
        assertThrows(IllegalArgumentException.class, () ->
                service.demanderCode("1234567", "etudiant@gmail.com", "perso@gmail.com"));
    }

    @Test
    void emailPersonnelInvalideRefuse() {
        assertThrows(IllegalArgumentException.class, () ->
                service.demanderCode("1234567", "etudiant@uadb.edu.sn", "pas-un-email"));
    }

    @Test
    void codeNonNumeriqueRefuse() {
        assertThrows(IllegalArgumentException.class, () ->
                service.verifierCode("1234567", "etudiant@uadb.edu.sn", "12ab56"));
    }

    @Test
    void ticketMalFormeRefuseAvantLaBase() {
        assertThrows(IllegalStateException.class, () ->
                service.definirMotDePasse("sans-point", "MotDePasseSolide", "MotDePasseSolide"));
        assertThrows(IllegalStateException.class, () ->
                service.definirMotDePasse("abc.jeton", "MotDePasseSolide", "MotDePasseSolide"));
    }

    @Test
    void motDePasseTropCourtRefuseAvantLaBase() {
        assertThrows(IllegalArgumentException.class, () ->
                service.definirMotDePasse("1.jeton", "court", "court"));
    }
}
