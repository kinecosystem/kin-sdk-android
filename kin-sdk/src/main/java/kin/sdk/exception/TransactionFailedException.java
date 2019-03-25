package kin.sdk.exception;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Blockchain transaction failure has happened, contains blockchain specific error details
 */
public class TransactionFailedException extends OperationFailedException {

    private final String txResultCode;
    private final List<String> opResultCode;

    public TransactionFailedException(@Nullable String txResultCode,
        @Nullable List<String> opResultCode) {
        super(getMessage(opResultCode));

        this.txResultCode = txResultCode;
        this.opResultCode = opResultCode;
    }

    @Nonnull
    private static String getMessage(@Nullable List<String> opResultCode) {
        return opResultCode != null && !opResultCode.isEmpty() ?
            "Transaction failed with the error = " + opResultCode.get(0) :
            "Transaction failed";
    }

    @Nullable
    public String getTransactionResultCode() {
        return txResultCode;
    }

    @Nullable
    public List<String> getOperationsResultCodes() {
        return opResultCode;
    }
}
