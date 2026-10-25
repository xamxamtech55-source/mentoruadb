package com.uadb.mentoruadb.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibiliteServiceTest {

    @Test
    void licence1NestPasEligible() {
        assertFalse(EligibiliteService.estNiveauEligible("Licence 1"));
        assertFalse(EligibiliteService.estNiveauEligible("  licence 1  "));
        assertFalse(EligibiliteService.estNiveauEligible("LICENCE 1"));
    }

    @Test
    void autresNiveauxSontEligibles() {
        assertTrue(EligibiliteService.estNiveauEligible("Licence 2"));
        assertTrue(EligibiliteService.estNiveauEligible("Licence 3"));
        assertTrue(EligibiliteService.estNiveauEligible("Master 1"));
        assertTrue(EligibiliteService.estNiveauEligible("Master 2"));
    }

    @Test
    void niveauAbsentNestPasEligible() {
        assertFalse(EligibiliteService.estNiveauEligible(null));
        assertFalse(EligibiliteService.estNiveauEligible("   "));
    }
}
