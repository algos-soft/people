package com.motork.people.exceptions;

public class MissingEmailException extends Exception{
    public MissingEmailException(String message) {
        super(message);
    }
}
