package kin.sdk;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import kin.base.responses.SubmitTransactionResponse;
import kin.base.responses.SubmitTransactionResponse.Extras.ResultCodes;
import kin.sdk.exception.TransactionFailedException;

final public class Utils {

    private Utils() {
        //no instances
    }

    static TransactionFailedException createTransactionException(@NonNull SubmitTransactionResponse response) {
        ArrayList<String> operationsResultCodes = null;
        String transactionResultCode = null;
        if (response.getExtras() != null && response.getExtras().getResultCodes() != null) {
            ResultCodes resultCodes = response.getExtras().getResultCodes();
            operationsResultCodes = resultCodes.getOperationsResultCodes();
            transactionResultCode = resultCodes.getTransactionResultCode();
        }
        return new TransactionFailedException(transactionResultCode, operationsResultCodes);
    }

    static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static void checkNotNull(Object obj, String paramName) {
        if (obj == null) {
            throw new IllegalArgumentException(paramName + " == null");
        }
    }

    static void checkNotEmpty(String string, String paramName) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be null or empty.");
        }
    }

    public static boolean isEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0;
    }
}
