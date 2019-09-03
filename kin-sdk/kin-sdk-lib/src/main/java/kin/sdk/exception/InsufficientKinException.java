package kin.sdk.exception;

/**
 * Transaction failed due to insufficient kin.
 */
public class InsufficientKinException extends OperationFailedException {

    public InsufficientKinException() {
        super("Not enough kin to perform the transaction.");
    }
}
