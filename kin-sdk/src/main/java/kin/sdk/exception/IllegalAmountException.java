package kin.sdk.exception;

/**
 * amount was not legal
 */
public class IllegalAmountException extends OperationFailedException {

    public IllegalAmountException(String errorReason) {
        super("Illegal amount - " + errorReason);
    }
}

