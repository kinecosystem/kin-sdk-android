package kin.sdk.exception;

public class InsufficientFeeException extends OperationFailedException {

    public InsufficientFeeException() {
        super("Not enough fee to perform the transaction.");
    }
}
