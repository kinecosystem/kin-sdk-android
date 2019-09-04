package kin.sdk.internal.blockchain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import kin.base.*;
import kin.base.Transaction.Builder;
import kin.base.responses.AccountResponse;
import kin.base.responses.HttpResponseException;
import kin.base.responses.SubmitTransactionResponse;
import kin.sdk.TransactionId;
import kin.sdk.exception.*;
import kin.sdk.internal.Utils;
import kin.sdk.internal.data.TransactionIdImpl;
import kin.sdk.transactiondata.PaymentTransaction;
import kin.sdk.transactiondata.Transaction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;

public class TransactionSender {

    private static final int MEMO_BYTES_LENGTH_LIMIT = 21; //Memo length limitation(in bytes) is 28 but we add 7 more bytes which includes the appId and some characters.
    private static final int MAX_NUM_OF_DECIMAL_PLACES = 4 ;
    private static String MEMO_APP_ID_VERSION_PREFIX = "1";
    private static String MEMO_DELIMITER = "-";
    private static final String INSUFFICIENT_KIN_RESULT_CODE = "op_underfunded";
    private static final String INSUFFICIENT_FEE_RESULT_CODE = "tx_insufficient_fee";
    private static final String INSUFFICIENT_BALANCE_RESULT_CODE = "tx_insufficient_balance";
    private final Server server; //horizon server
    private final String appId;

    public TransactionSender(Server server, String appId) {
        this.server = server;
        this.appId = appId;
    }

    public PaymentTransaction buildTransaction(@NonNull KeyPair from, @NonNull String publicAddress,
                                               @NonNull BigDecimal amount,
                                               int fee) throws OperationFailedException {
        return buildTransaction(from, publicAddress, amount, fee, null);
    }

    public PaymentTransaction buildTransaction(@NonNull KeyPair from, @NonNull String publicAddress, @NonNull BigDecimal amount,
                                               int fee, @Nullable String memo) throws OperationFailedException {
        checkParams(from, publicAddress, amount, fee, memo);
        if (appId != null && !appId.equals("")) {
            memo = addAppIdToMemo(memo);
        }

        KeyPair addressee = generateAddresseeKeyPair(publicAddress);
        AccountResponse sourceAccount = loadSourceAccount(from);
        verifyAddresseeAccount(generateAddresseeKeyPair(addressee.getAccountId()));
        kin.base.Transaction stellarTransaction = buildStellarTransaction(from, amount, addressee, sourceAccount, fee
                , memo);
        return new PaymentTransaction(stellarTransaction, addressee.getAccountId(), amount, memo);
    }

    public TransactionId sendTransaction(Transaction transaction) throws OperationFailedException {
        return sendTransaction(((TransactionInternal) transaction).baseTransaction());
    }

    public TransactionId sendWhitelistTransaction(String whitelist) throws OperationFailedException {
        try {
            kin.base.Transaction transaction = kin.base.Transaction.fromEnvelopeXdr(whitelist);
            return sendTransaction(transaction);
        } catch (IOException e) {
            throw new OperationFailedException("whitelist transaction data invalid", e);
        }
    }

    @NonNull
    private String addAppIdToMemo(@Nullable String memo) {
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

    private void checkParams(@NonNull KeyPair from, @NonNull String publicAddress, @NonNull BigDecimal amount,
                             int fee, @Nullable String memo) throws OperationFailedException {
        Utils.checkNotNull(from, "account");
        Utils.checkNotNull(amount, "amount");
        validateAmountDecimalPoint(amount);
        checkForNegativeFee(fee);
        checkAddressNotEmpty(publicAddress);
        checkForNegativeAmount(amount);
        checkMemo(memo);
    }


    private void validateAmountDecimalPoint(BigDecimal amount) throws OperationFailedException {
        BigDecimal amountWithoutTrailingZeros = amount.stripTrailingZeros();
        int numOfDecimalPlaces = amountWithoutTrailingZeros.scale();
        if (numOfDecimalPlaces > MAX_NUM_OF_DECIMAL_PLACES) {
            throw new IllegalAmountException("amount can't have more then 5 digits after the decimal point");
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void checkAddressNotEmpty(@NonNull String publicAddress) {
        if (publicAddress == null || publicAddress.isEmpty()) {
            throw new IllegalArgumentException("Addressee not valid - public address can't be null or empty");
        }
    }

    private void checkForNegativeAmount(@NonNull BigDecimal amount) {
        if (amount.signum() == -1) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
    }

    private void checkForNegativeFee(int fee) {
        if (fee < 0) {
            throw new IllegalArgumentException("Fee can't be negative");
        }
    }

    private void checkMemo(String memo) {
        try {
            if (memo != null && memo.getBytes("UTF-8").length > MEMO_BYTES_LENGTH_LIMIT) {
                throw new IllegalArgumentException("Memo cannot be longer that " + MEMO_BYTES_LENGTH_LIMIT + " bytes(UTF-8 characters)");
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Memo text have unsupported characters encoding");
        }
    }

    @NonNull
    private KeyPair generateAddresseeKeyPair(@NonNull String publicAddress) throws OperationFailedException {
        try {
            return KeyPair.fromAccountId(publicAddress);
        } catch (Exception e) {
            throw new OperationFailedException("Invalid addressee public address format", e);
        }
    }

    @NonNull
    private kin.base.Transaction buildStellarTransaction(@NonNull KeyPair from, @NonNull BigDecimal amount, KeyPair addressee,
                                                         AccountResponse sourceAccount, int fee, @Nullable String memo) {
        Builder transactionBuilder = new Builder(sourceAccount)
                .addOperation(
                        new PaymentOperation.Builder(addressee, new AssetTypeNative(), amount.toString()).build());
        transactionBuilder.addFee(fee);
        if (memo != null) {
            transactionBuilder.addMemo(Memo.text(memo));
        }
        kin.base.Transaction transaction = transactionBuilder.build();
        transaction.sign(from);
        return transaction;
    }

    private void verifyAddresseeAccount(KeyPair addressee) throws OperationFailedException {
        loadAccount(addressee);
    }

    private AccountResponse loadAccount(@NonNull KeyPair from) throws OperationFailedException {
        AccountResponse sourceAccount;
        try {
            sourceAccount = server.accounts().account(from);
        } catch (HttpResponseException httpError) {
            if (httpError.getStatusCode() == 404) {
                throw new AccountNotFoundException(from.getAccountId());
            } else {
                throw new OperationFailedException(httpError);
            }
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
        if (sourceAccount == null) {
            throw new OperationFailedException("can't retrieve data for account " + from.getAccountId());
        }
        return sourceAccount;
    }

    private AccountResponse loadSourceAccount(@NonNull KeyPair from) throws OperationFailedException {
        AccountResponse sourceAccount;
        sourceAccount = loadAccount(from);
        return sourceAccount;
    }

    @NonNull
    private TransactionId sendTransaction(kin.base.Transaction transaction) throws OperationFailedException {
        try {
            SubmitTransactionResponse response = server.submitTransaction(transaction);
            if (response == null) {
                throw new OperationFailedException("can't get transaction response");
            }
            if (response.isSuccess()) {
                return new TransactionIdImpl(response.getHash());
            } else {
                return createFailureException(response);
            }
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
    }

    private TransactionId createFailureException(SubmitTransactionResponse response)
            throws TransactionFailedException, InsufficientKinException, InsufficientFeeException {
        TransactionFailedException transactionException = Utils.createTransactionException(response);
        if (isInsufficientKinException(transactionException)) {
            throw new InsufficientKinException();
        } else if (isInsufficientFeeException(transactionException)) {
            throw new InsufficientFeeException();
        } else {
            throw transactionException;
        }
    }

    private boolean isInsufficientKinException(TransactionFailedException transactionException) {
        List<String> resultCodes = transactionException.getOperationsResultCodes();
        String transactionResultCode = transactionException.getTransactionResultCode();
        return ((resultCodes != null && resultCodes.size() > 0 && INSUFFICIENT_KIN_RESULT_CODE.equals(resultCodes.get(0))) ||
                !TextUtils.isEmpty(transactionResultCode) && INSUFFICIENT_BALANCE_RESULT_CODE.equals(transactionResultCode));
    }

    private boolean isInsufficientFeeException(TransactionFailedException transactionException) {
        String transactionResultCode = transactionException.getTransactionResultCode();
        return !TextUtils.isEmpty(transactionResultCode) && INSUFFICIENT_FEE_RESULT_CODE.equals(transactionResultCode);
    }

}
