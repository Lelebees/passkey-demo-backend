package nl.lelebees.passkeydemo.backend.domain;

public class IncorrectEmailFormatException extends Exception {
    public IncorrectEmailFormatException(String s) {
        super(s);
    }
}
