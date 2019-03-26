package kin.sdk;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import kin.base.AssetTypeNative;
import kin.base.KeyPair;
import kin.base.Memo;
import kin.base.Network;
import kin.base.PaymentOperation;
import kin.base.Server;
import kin.base.Transaction.Builder;
import kin.base.codec.StringUtils;
import kin.base.responses.AccountResponse;
import kin.base.responses.HttpResponseException;
import kin.base.responses.SubmitTransactionResponse;
import kin.sdk.exception.AccountNotFoundException;
import kin.sdk.exception.IllegalAmountException;
import kin.sdk.exception.InsufficientFeeException;
import kin.sdk.exception.InsufficientKinException;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.exception.TransactionFailedException;

class TransactionSender
{

    private static final int MEMO_BYTES_LENGTH_LIMIT = 21; //Memo length limitation(in bytes) is 28 but we add 7 more bytes which includes the appId and some characters.
    private static final int MAX_NUM_OF_DECIMAL_PLACES = 4;
    private static String MEMO_APP_ID_VERSION_PREFIX = "1";
    private static String MEMO_DELIMITER = "-";
    private static final String INSUFFICIENT_KIN_RESULT_CODE = "op_underfunded";
    private static final String INSUFFICIENT_FEE_RESULT_CODE = "tx_insufficient_fee";
    private static final String INSUFFICIENT_BALANCE_RESULT_CODE = "tx_insufficient_balance";
    private final Server server; //horizon server
    private final String appId;

    TransactionSender(Server server, String appId)
    {
        this.server = server;
        this.appId = appId;
    }

    Transaction buildTransaction(@Nonnull KeyPair from, @Nonnull String publicAddress, @Nonnull BigDecimal amount,
            int fee) throws OperationFailedException
    {
        return buildTransaction(from, publicAddress, amount, fee, null);
    }

    Transaction buildTransaction(@Nonnull KeyPair from, @Nonnull String publicAddress, @Nonnull BigDecimal amount,
            int fee, @Nullable String memo) throws OperationFailedException
    {
        checkParams(from, publicAddress, amount, fee, memo);
        memo = addAppIdToMemo(memo);

        KeyPair addressee = generateAddresseeKeyPair(publicAddress);
        AccountResponse sourceAccount = loadSourceAccount(from);
        verifyAddresseeAccount(generateAddresseeKeyPair(addressee.getAccountId()));
        kin.base.Transaction stellarTransaction = buildStellarTransaction(from, amount, addressee, sourceAccount, fee, memo);
        TransactionId id = new TransactionIdImpl(Utils.byteArrayToHex(stellarTransaction.hash()));
        WhitelistableTransaction whitelistableTransaction =
                new WhitelistableTransaction(stellarTransaction.toEnvelopeXdrBase64(), Network.current().getNetworkPassphrase());
        return new Transaction(addressee, from, amount, fee, memo, id, stellarTransaction, whitelistableTransaction);
    }

    TransactionId sendTransaction(Transaction transaction) throws OperationFailedException
    {
        return sendTransaction(transaction.getStellarTransaction());
    }

    TransactionId sendWhitelistTransaction(String whitelist) throws OperationFailedException
    {
        try {
            kin.base.Transaction transaction = kin.base.Transaction.fromEnvelopeXdr(whitelist);
            return sendTransaction(transaction);
        }
        catch (IOException e) {
            throw new OperationFailedException("whitelist transaction data invalid", e);
        }
    }

    @Nonnull
    private String addAppIdToMemo(@Nullable String memo)
    {
        if (memo == null) {
            memo = "";
        }
        else {
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

    private void checkParams(@Nonnull KeyPair from, @Nonnull String publicAddress, @Nonnull BigDecimal amount,
            int fee, @Nullable String memo) throws OperationFailedException
    {
        Utils.checkNonnull(from, "account");
        Utils.checkNonnull(amount, "amount");
        validateAmountDecimalPoint(amount);
        checkForNegativeFee(fee);
        checkAddressNotEmpty(publicAddress);
        checkForNegativeAmount(amount);
        checkMemo(memo);
    }


    private void validateAmountDecimalPoint(BigDecimal amount) throws OperationFailedException
    {
        BigDecimal amountWithoutTrailingZeros = amount.stripTrailingZeros();
        int numOfDecimalPlaces = amountWithoutTrailingZeros.scale();
        if (numOfDecimalPlaces > MAX_NUM_OF_DECIMAL_PLACES) {
            throw new IllegalAmountException("amount can't have more then 5 digits after the decimal point");
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void checkAddressNotEmpty(@Nonnull String publicAddress)
    {
        if (publicAddress == null || publicAddress.isEmpty()) {
            throw new IllegalArgumentException("Addressee not valid - public address can't be null or empty");
        }
    }

    private void checkForNegativeAmount(@Nonnull BigDecimal amount)
    {
        if (amount.signum() == -1) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
    }

    private void checkForNegativeFee(int fee)
    {
        if (fee < 0) {
            throw new IllegalArgumentException("Fee can't be negative");
        }
    }

    private void checkMemo(String memo)
    {
        try {
            if (memo != null && memo.getBytes("UTF-8").length > MEMO_BYTES_LENGTH_LIMIT) {
                throw new IllegalArgumentException("Memo cannot be longer that " + MEMO_BYTES_LENGTH_LIMIT + " bytes(UTF-8 characters)");
            }
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Memo text have unsupported characters encoding");
        }
    }

    @Nonnull
    private KeyPair generateAddresseeKeyPair(@Nonnull String publicAddress) throws OperationFailedException
    {
        try {
            return KeyPair.fromAccountId(publicAddress);
        }
        catch (Exception e) {
            throw new OperationFailedException("Invalid addressee public address format", e);
        }
    }

    @Nonnull
    private kin.base.Transaction buildStellarTransaction(@Nonnull KeyPair from, @Nonnull BigDecimal amount, KeyPair addressee,
            AccountResponse sourceAccount, int fee, @Nullable String memo)
    {
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

    private void verifyAddresseeAccount(KeyPair addressee) throws OperationFailedException
    {
        loadAccount(addressee);
    }

    private AccountResponse loadAccount(@Nonnull KeyPair from) throws OperationFailedException
    {
        AccountResponse sourceAccount;
        try {
            sourceAccount = server.accounts().account(from);
        }
        catch (HttpResponseException httpError) {
            if (httpError.getStatusCode() == 404) {
                throw new AccountNotFoundException(from.getAccountId());
            }
            else {
                throw new OperationFailedException(httpError);
            }
        }
        catch (IOException e) {
            throw new OperationFailedException(e);
        }
        if (sourceAccount == null) {
            throw new OperationFailedException("can't retrieve data for account " + from.getAccountId());
        }
        return sourceAccount;
    }

    private AccountResponse loadSourceAccount(@Nonnull KeyPair from) throws OperationFailedException
    {
        AccountResponse sourceAccount;
        sourceAccount = loadAccount(from);
        return sourceAccount;
    }

    @Nonnull
    private TransactionId sendTransaction(kin.base.Transaction transaction) throws OperationFailedException
    {
        try {
            SubmitTransactionResponse response = server.submitTransaction(transaction);
            if (response == null) {
                throw new OperationFailedException("can't get transaction response");
            }
            if (response.isSuccess()) {
                return new TransactionIdImpl(response.getHash());
            }
            else {
                return createFailureException(response);
            }
        }
        catch (IOException e) {
            throw new OperationFailedException(e);
        }
    }

    private TransactionId createFailureException(SubmitTransactionResponse response)
            throws TransactionFailedException, InsufficientKinException, InsufficientFeeException
    {
        TransactionFailedException transactionException = Utils.createTransactionException(response);
        if (isInsufficientKinException(transactionException)) {
            throw new InsufficientKinException();
        }
        else if (isInsufficientFeeException(transactionException)) {
            throw new InsufficientFeeException();
        }
        else {
            throw transactionException;
        }
    }

    private boolean isInsufficientKinException(TransactionFailedException transactionException)
    {
        List<String> resultCodes = transactionException.getOperationsResultCodes();
        String transactionResultCode = transactionException.getTransactionResultCode();
        return ((resultCodes != null && resultCodes.size() > 0 && INSUFFICIENT_KIN_RESULT_CODE.equals(resultCodes.get(0))) ||
                !StringUtils.isEmpty(transactionResultCode) && INSUFFICIENT_BALANCE_RESULT_CODE.equals(transactionResultCode));
    }

    private boolean isInsufficientFeeException(TransactionFailedException transactionException)
    {
        String transactionResultCode = transactionException.getTransactionResultCode();
        return !StringUtils.isEmpty(transactionResultCode) && INSUFFICIENT_FEE_RESULT_CODE.equals(transactionResultCode);
    }
}
