package Exceptions;

public class CriticalSystemFailureException extends RuntimeException {
    public CriticalSystemFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}