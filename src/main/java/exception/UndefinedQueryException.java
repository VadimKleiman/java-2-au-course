package exception;

public class UndefinedQueryException extends Exception {

    public UndefinedQueryException(String message) {
        super(message);
    }

    public UndefinedQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
