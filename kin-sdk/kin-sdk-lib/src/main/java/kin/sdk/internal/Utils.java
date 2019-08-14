package kin.sdk.internal;


import android.support.annotation.NonNull;
import kin.base.responses.SubmitTransactionResponse;
import kin.base.responses.SubmitTransactionResponse.Extras.ResultCodes;
import kin.sdk.exception.TransactionFailedException;

import java.util.ArrayList;

public final class Utils {

    private Utils() {
        //no instances
    }

    public static TransactionFailedException createTransactionException(@NonNull SubmitTransactionResponse response) {
        ArrayList<String> operationsResultCodes = null;
        String transactionResultCode = null;
        if (response.getExtras() != null && response.getExtras().getResultCodes() != null) {
            ResultCodes resultCodes = response.getExtras().getResultCodes();
            operationsResultCodes = resultCodes.getOperationsResultCodes();
            transactionResultCode = resultCodes.getTransactionResultCode();
        }
        return new TransactionFailedException(transactionResultCode, operationsResultCodes);
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static void checkNotNull(Object obj, String paramName) {
        if (obj == null) {
            throw new IllegalArgumentException(paramName + " == null");
        }
    }

    public static void checkNotEmpty(String string, String paramName) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be null or empty.");
        }
    }
}
