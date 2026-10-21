package com.abcgreen.evnet.service;

/** Custom checked exceptions (Module III: exception handling). */
public class Exceptions {

    public static class DuplicateRecordException extends RuntimeException {
        public DuplicateRecordException(String what) { super(what + " already exists"); }
    }
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String what) { super(what + " not found"); }
    }
    public static class AuthException extends RuntimeException {
        public AuthException(String msg) { super(msg); }
    }
    public static class SlotUnavailableException extends RuntimeException {
        public SlotUnavailableException(String msg) { super(msg); }
    }

    private Exceptions() {}
}
