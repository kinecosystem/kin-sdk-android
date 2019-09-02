package kin.sdk.internal;


import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;

import kin.base.responses.SubmitTransactionResponse;
import kin.base.responses.SubmitTransactionResponse.Extras.ResultCodes;
import kin.sdk.exception.TransactionFailedException;

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

    public static void checkForNegativeFee(int fee) {
        if (fee < 0) {
            throw new IllegalArgumentException("Fee can't be negative");
        }
    }

    public static void checkForNegativeAmount(@NonNull BigDecimal amount) {
        if (amount.signum() == -1) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void checkAddressNotEmpty(@NonNull String publicAddress) {
        if (publicAddress == null || publicAddress.isEmpty()) {
            throw new IllegalArgumentException("Addressee not valid - public address can't be " +
                    "null or empty");
        }
    }
}
