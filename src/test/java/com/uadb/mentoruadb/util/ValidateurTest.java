package com.uadb.mentoruadb.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidateurTest {

    @Test
    void unNumeroDeCarteDoitAvoirExactementSeptChiffres() {
        assertTrue(Validateur.numeroCarteValide("1234567"));
        assertTrue(Validateur.numeroCarteValide(" 7654321 "));
        assertFalse(Validateur.numeroCarteValide("123456"));
        assertFalse(Validateur.numeroCarteValide("12345678"));
        assertFalse(Validateur.numeroCarteValide("12345AB"));
        assertFalse(Validateur.numeroCarteValide(null));
        assertFalse(Validateur.numeroCarteValide(""));
    }

    @Test
    void unTelephoneSenegalaisValideEstAccepte() {
        assertTrue(Validateur.telephoneValide("771234567"));
        assertTrue(Validateur.telephoneValide("77 123 45 67"));
        assertTrue(Validateur.telephoneValide("77-123-45-67"));
        assertTrue(Validateur.telephoneValide("+221 77 123 45 67"));
        assertTrue(Validateur.telephoneValide("33 123 45 67"));
    }

    @Test
    void unTelephoneInvalideEstRefuse() {
        assertFalse(Validateur.telephoneValide("77123456"));
        assertFalse(Validateur.telephoneValide("7712345678"));
        assertFalse(Validateur.telephoneValide("791234567"));
        assertFalse(Validateur.telephoneValide("abcdefghi"));
        assertFalse(Validateur.telephoneValide(null));
        assertFalse(Validateur.telephoneValide(""));
    }

    @Test
    void leTelephoneEstStockeAvecNeufChiffres() {
        assertEquals("771234567", Validateur.normaliserTelephone("77 123 45 67"));
        assertEquals("771234567", Validateur.normaliserTelephone("+221 77 123 45 67"));
        assertEquals("771234567", Validateur.normaliserTelephone("00221 77 123 45 67"));
        assertEquals("", Validateur.normaliserTelephone(null));
    }

    @Test
    void leTelephoneEstFormatePourLAffichage() {
        assertEquals("77 123 45 67", Validateur.formaterTelephone("771234567"));
        assertEquals("77 123 45 67", Validateur.formaterTelephone("+221771234567"));
        assertEquals("", Validateur.formaterTelephone(null));
    }

    @Test
    void leTelephoneEstFormateProgressivementPendantLaSaisie() {
        assertEquals("", Validateur.formaterTelephonePartiel(""));
        assertEquals("7", Validateur.formaterTelephonePartiel("7"));
        assertEquals("77", Validateur.formaterTelephonePartiel("77"));
        assertEquals("77 1", Validateur.formaterTelephonePartiel("771"));
        assertEquals("77 123", Validateur.formaterTelephonePartiel("77123"));
        assertEquals("77 123 45", Validateur.formaterTelephonePartiel("7712345"));
        assertEquals("77 123 45 6", Validateur.formaterTelephonePartiel("77123456"));
        assertEquals("77 123 45 67", Validateur.formaterTelephonePartiel("771234567"));
        assertEquals("77 123 45 67", Validateur.formaterTelephonePartiel("7712345678"));
    }

    @Test
    void unEmailDoitAvoirLaFormeAttendue() {
        assertTrue(Validateur.emailValide("prenom.nom@uadb.edu.sn"));
        assertTrue(Validateur.emailValide("etudiant@gmail.com"));
        assertFalse(Validateur.emailValide("prenom.nom"));
        assertFalse(Validateur.emailValide("prenom@uadb"));
        assertFalse(Validateur.emailValide(null));
    }
}
