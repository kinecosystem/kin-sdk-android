package kin.sdk.internal.services;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.List;

import kin.base.AccountLedgerEntryChange;
import kin.base.Asset;
import kin.base.KeyPair;
import kin.base.LedgerEntryChange;
import kin.base.LedgerEntryChanges;
import kin.base.Memo;
import kin.base.MemoText;
import kin.base.Operation;
import kin.base.PaymentOperation;
import kin.base.Server;
import kin.base.responses.TransactionResponse;
import kin.sdk.internal.services.helpers.EventListener;
import kin.sdk.internal.services.helpers.ListenerRegistration;
import kin.sdk.internal.services.helpers.ManagedServerSentEventStream;
import kin.sdk.models.Balance;
import kin.sdk.models.PaymentInfo;
import kin.sdk.models.TransactionId;

import static kin.sdk.internal.utils.Utils.checkNotNull;

/**
 * Provides listeners, for various events happens on the blockchain.
 */
public final class BlockchainEventsImpl implements BlockchainEvents {

    private static final String ASSET_TYPE_NATIVE = "native";
    private static final String CURSOR_FUTURE_ONLY = "now";

    private final KeyPair accountKeyPair;
    private final ManagedServerSentEventStream<TransactionResponse> transactionsStream;

    public BlockchainEventsImpl(Server server, String accountId) {
        this.accountKeyPair = KeyPair.fromAccountId(accountId);

        this.transactionsStream = new ManagedServerSentEventStream<>(
                server.transactions()
                        .forAccount(this.accountKeyPair)
                        .cursor(CURSOR_FUTURE_ONLY)
        );
    }

    @Override
    public ListenerRegistration addBalanceListener(@NonNull final EventListener<Balance> listener) {
        checkNotNull(listener, "listener");

        final kin.base.requests.EventListener<TransactionResponse> responseListener = new kin.base.requests.EventListener<TransactionResponse>() {
            @Override
            public void onEvent(TransactionResponse transactionResponse) {
                extractBalanceChangeFromTransaction(transactionResponse, listener);
            }
        };

        transactionsStream.addListener(responseListener);

        return new ListenerRegistration(new Runnable() {
            @Override
            public void run() {
                transactionsStream.removeListener(responseListener);
            }
        });
    }

    private void extractBalanceChangeFromTransaction(TransactionResponse transactionResponse,
                                                     @NonNull EventListener<Balance> listener) {
        List<LedgerEntryChanges> ledgerChanges = transactionResponse.getLedgerChanges();
        if (ledgerChanges != null) {
            for (LedgerEntryChanges ledgerChange : ledgerChanges) {
                LedgerEntryChange[] ledgerEntryUpdates = ledgerChange.getLedgerEntryUpdates();
                if (ledgerEntryUpdates != null) {
                    for (LedgerEntryChange ledgerEntryUpdate : ledgerEntryUpdates) {
                        extractBalanceFromUpdate(listener, ledgerEntryUpdate);
                    }
                }
            }
        }
    }

    private void extractBalanceFromUpdate(@NonNull EventListener<Balance> listener,
                                          LedgerEntryChange ledgerEntryUpdate) {
        if (ledgerEntryUpdate instanceof AccountLedgerEntryChange) {
            AccountLedgerEntryChange accountLedgerEntryChange = (AccountLedgerEntryChange) ledgerEntryUpdate;
            KeyPair account = accountLedgerEntryChange.getAccount();
            if (account != null) {
                if (accountKeyPair.getAccountId().equals(account.getAccountId())) {
                    Balance balance = new Balance(
                            new BigDecimal(accountLedgerEntryChange.getBalance()));
                    listener.onEvent(balance);
                }
            }
        }
    }

    @Override
    public ListenerRegistration addPaymentListener(@NonNull final EventListener<PaymentInfo> listener) {
        checkNotNull(listener, "listener");

        final kin.base.requests.EventListener<TransactionResponse> responseListener = new kin.base.requests.EventListener<TransactionResponse>() {
            @Override
            public void onEvent(TransactionResponse transactionResponse) {
                extractPaymentsFromTransaction(transactionResponse, listener);
            }
        };

        transactionsStream.addListener(responseListener);

        return new ListenerRegistration(new Runnable() {
            @Override
            public void run() {
                transactionsStream.removeListener(responseListener);
            }
        });
    }

    @Override
    public ListenerRegistration addAccountCreationListener(final EventListener<Void> listener) {
        checkNotNull(listener, "listener");

        final kin.base.requests.EventListener<TransactionResponse> responseListener = new kin.base.requests.EventListener<TransactionResponse>() {
            private boolean eventOccurred = false;

            @Override
            public void onEvent(TransactionResponse transactionResponse) {
                //account creation is one time operation, fire event only once
                if (!eventOccurred) {
                    eventOccurred = true;
                    listener.onEvent(null);
                }
            }
        };

        transactionsStream.addListener(responseListener);

        return new ListenerRegistration(new Runnable() {
            @Override
            public void run() {
                transactionsStream.removeListener(responseListener);
            }
        });
    }

    private void extractPaymentsFromTransaction(TransactionResponse transactionResponse,
                                                EventListener<PaymentInfo> listener) {
        List<Operation> operations = transactionResponse.getOperations();
        if (operations != null) {
            for (Operation operation : operations) {
                if (operation instanceof PaymentOperation) {
                    PaymentOperation paymentOperation = (PaymentOperation) operation;
                    if (isPaymentNative(paymentOperation.getAsset())) {
                        PaymentInfo paymentInfo = new PaymentInfo(
                                transactionResponse.getCreatedAt(),
                                paymentOperation.getDestination().getAccountId(),
                                extractSourceAccountId(transactionResponse, paymentOperation),
                                new BigDecimal(paymentOperation.getAmount()),
                                new TransactionId(transactionResponse.getHash()),
                                transactionResponse.getFeePaid(),
                                extractHashTextIfAny(transactionResponse)
                        );
                        listener.onEvent(paymentInfo);
                    }
                }
            }

        }
    }

    private boolean isPaymentNative(Asset asset) {
        return asset != null && asset.getType().equalsIgnoreCase(ASSET_TYPE_NATIVE);
    }

    private String extractSourceAccountId(TransactionResponse transactionResponse, Operation operation) {
        //if payment was sent on behalf of other account - paymentOperation will contains this account, o.w. the source
        //is the transaction source account
        return operation.getSourceAccount() != null ? operation.getSourceAccount()
                .getAccountId() : transactionResponse.getSourceAccount().getAccountId();
    }

    private String extractHashTextIfAny(TransactionResponse transactionResponse) {
        String memoString = null;
        Memo memo = transactionResponse.getMemo();
        if (memo instanceof MemoText) {
            memoString = ((MemoText) memo).getText();
        }
        return memoString;
    }
}
