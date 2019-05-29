package kin.sdk.exception;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.List;

/**
 * Blockchain transaction failure has happened, contains blockchain specific error details
 */
public class TransactionFailedException extends OperationFailedException {

    private final String txResultCode;
    private final List<String> opResultCode;

    public TransactionFailedException(@Nullable String txResultCode,
        @Nullable List<String> opResultCode) {
        super(getMessage(txResultCode, opResultCode));

        this.txResultCode = txResultCode;
        this.opResultCode = opResultCode;
    }

    @NonNull
    private static String getMessage(@Nullable String txResultCode, @Nullable List<String> opResultCode) {
        return opResultCode != null && !opResultCode.isEmpty() ?
            "Transaction failed with the error = " + txResultCode + ", operations = " + opResultCode :
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
