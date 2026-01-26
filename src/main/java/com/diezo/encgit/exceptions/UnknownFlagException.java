package com.diezo.encgit.exceptions;

public class UnknownFlagException extends RuntimeException {
    public UnknownFlagException() {
        super("Looks like you've passed an unknown flag");
    }
}
