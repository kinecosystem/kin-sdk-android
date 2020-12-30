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
<<<<<<< HEAD
        @Nullable List<String> opResultCode) {
        super(getMessage(txResultCode, opResultCode));
=======
                                      @Nullable List<String> opResultCode) {
        super(getMessage(opResultCode));
>>>>>>> master

        this.txResultCode = txResultCode;
        this.opResultCode = opResultCode;
    }

    @NonNull
<<<<<<< HEAD
    private static String getMessage(@Nullable String txResultCode, @Nullable List<String> opResultCode) {
        String opResultCodeErrorMessage =
            opResultCode != null && !opResultCode.isEmpty() ? ", operations = " + opResultCode : "";
        return "Transaction failed with the error = " + txResultCode + opResultCodeErrorMessage;


=======
    private static String getMessage(@Nullable List<String> opResultCode) {
        return opResultCode != null && !opResultCode.isEmpty() ?
                "Transaction failed with the error = " + opResultCode.get(0) :
                "Transaction failed";
>>>>>>> master
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
