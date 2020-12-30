package kin.sdk;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
<<<<<<< HEAD
import java.io.UnsupportedEncodingException;
=======

>>>>>>> master
import java.util.ArrayList;

import kin.base.responses.SubmitTransactionResponse;
import kin.base.responses.SubmitTransactionResponse.Extras.ResultCodes;
import kin.sdk.exception.TransactionFailedException;

final public class Utils {

    private static final int MEMO_BYTES_LENGTH_LIMIT = 21; //Memo length limitation(in bytes) is 28 but we add 7 more bytes which includes the appId and some characters.
    private static String MEMO_APP_ID_VERSION_PREFIX = "1";
    private static String MEMO_DELIMITER = "-";
    private static String UTF_8 = "UTF-8";

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
        for (byte b : a)
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

<<<<<<< HEAD
    @NonNull
    static String addAppIdToMemo(String appId, @Nullable String memo) {
        if (memo == null) {
            memo = "";
        } else {
            memo = memo.trim(); // remove leading and trailing whitespaces.
        }
        StringBuilder sb = new StringBuilder();
        sb.append(MEMO_APP_ID_VERSION_PREFIX)
            .append(MEMO_DELIMITER)
            .append(appId)
            .append(MEMO_DELIMITER)
            .append(memo);
        return sb.toString();
    }

    static void validateMemo(String memo) {
        try {
            if (memo != null && memo.getBytes(UTF_8).length > MEMO_BYTES_LENGTH_LIMIT) {
                throw new IllegalArgumentException(
                    "Memo cannot be longer that " + MEMO_BYTES_LENGTH_LIMIT + " bytes(UTF-8 characters)");
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Memo text have unsupported characters encoding");
        }
=======
    public static boolean isEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0;
>>>>>>> master
    }
}
