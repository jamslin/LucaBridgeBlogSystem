package com.lucabridge.core.event;

import java.security.SecureRandom;

/** Shown on-screen and read aloud at check-in, so the alphabet excludes 0/O, 1/I/L. */
final class ReferenceCodeGenerator {

    private static final String ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private ReferenceCodeGenerator() {
    }

    static String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
