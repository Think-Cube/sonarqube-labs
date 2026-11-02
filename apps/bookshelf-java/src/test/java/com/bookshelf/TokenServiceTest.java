package com.bookshelf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class TokenServiceTest {

    private final TokenService service = new TokenService();

    @Test
    void hashIsHexEncoded() {
        assertEquals("900150983cd24fb0d6963f7d28e17f72", service.hashPassword("abc"));
    }

    @Test
    void tokensDiffer() {
        assertNotEquals(service.newSessionToken(), service.newSessionToken());
    }
}
