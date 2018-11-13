package kin.sdk.exception;

public class InsufficientBalanceException extends OperationFailedException {

    public InsufficientBalanceException() {
        super("Not enough balance to perform the transaction.");
    }
}
