package com.uadb.mentoruadb.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void unMotDePasseHacheEstVerifiable() {
        String hash = PasswordUtil.hacher("Secret#2026");
        assertTrue(PasswordUtil.verifier("Secret#2026", hash));
    }

    @Test
    void unMauvaisMotDePasseEstRefuse() {
        String hash = PasswordUtil.hacher("Secret#2026");
        assertFalse(PasswordUtil.verifier("secret#2026", hash));
        assertFalse(PasswordUtil.verifier("", hash));
    }

    @Test
    void lHashNeContientPasLeMotDePasseEtChangeACHaqueFois() {
        String h1 = PasswordUtil.hacher("motdepasse");
        String h2 = PasswordUtil.hacher("motdepasse");
        assertFalse(h1.contains("motdepasse"));
        assertNotEquals(h1, h2, "le sel doit être différent à chaque hachage");
        assertTrue(PasswordUtil.verifier("motdepasse", h1));
        assertTrue(PasswordUtil.verifier("motdepasse", h2));
    }

    @Test
    void unAncienMotDePasseEnClairEstAccepteEtMarqueAMettreAJour() {
        assertTrue(PasswordUtil.verifier("password123", "password123"));
        assertFalse(PasswordUtil.verifier("autre", "password123"));
        assertFalse(PasswordUtil.estHache("password123"));
        assertTrue(PasswordUtil.doitEtreMisAJour("password123"));
    }

    @Test
    void unHashAJourNeDemandePasDeMiseAJour() {
        String hash = PasswordUtil.hacher("abc");
        assertTrue(PasswordUtil.estHache(hash));
        assertFalse(PasswordUtil.doitEtreMisAJour(hash));
    }

    @Test
    void uneValeurStockeeCorrompueEstRefuseeSansException() {
        assertFalse(PasswordUtil.verifier("abc", "pbkdf2$xx$!!$??"));
        assertFalse(PasswordUtil.verifier(null, "password123"));
        assertFalse(PasswordUtil.verifier("abc", null));
    }
}
