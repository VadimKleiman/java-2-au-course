package exception;

public class IOQueryException extends Exception {
    public IOQueryException(String message) {
        super(message);
    }

    public IOQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
