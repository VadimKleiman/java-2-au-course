package ru.spbau.mit;

public class LightExecutionException extends RuntimeException {
    public LightExecutionException(String message, Throwable th) {
        super(message, th);
    }
}
