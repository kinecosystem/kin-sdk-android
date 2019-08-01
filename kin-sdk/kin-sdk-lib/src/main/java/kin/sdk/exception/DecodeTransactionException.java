package kin.sdk.exception;

public class DecodeTransactionException extends Exception {

    public DecodeTransactionException(String message) {
        super(message);
    }

    public DecodeTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodeTransactionException(Throwable cause) {
        super(cause);
    }

}
