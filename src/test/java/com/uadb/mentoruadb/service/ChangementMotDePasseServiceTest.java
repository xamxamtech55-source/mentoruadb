package com.uadb.mentoruadb.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChangementMotDePasseServiceTest {

    @Test
    void motDePasseValideEstAccepte() {
        assertDoesNotThrow(() ->
                ChangementMotDePasseService.validerNouveauMotDePasse("MotDePasseSolide", "MotDePasseSolide"));
    }

    @Test
    void champsVidesRefuses() {
        assertThrows(IllegalArgumentException.class, () ->
                ChangementMotDePasseService.validerNouveauMotDePasse("", ""));
        assertThrows(IllegalArgumentException.class, () ->
                ChangementMotDePasseService.validerNouveauMotDePasse("MotDePasseSolide", " "));
    }

    @Test
    void confirmationDifferenteRefusee() {
        assertThrows(IllegalArgumentException.class, () ->
                ChangementMotDePasseService.validerNouveauMotDePasse("MotDePasseSolide", "AutreMotDePasse"));
    }

    @Test
    void motDePasseTropCourtRefuse() {
        assertThrows(IllegalArgumentException.class, () ->
                ChangementMotDePasseService.validerNouveauMotDePasse("court", "court"));
    }
}
