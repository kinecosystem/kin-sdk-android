package kin.sdk;

import kin.sdk.queue.TransactionProcess;

/**
 * provide the generated transaction before sending to the blockchain.
 */
public interface TransactionInterceptor<T extends TransactionProcess> {

    /**
     * Intercept the generated transaction before sending to the blockchain
     * Will be fired when the transaction is ready, and before it will be sending to the blockchain.
     * <p> Note - This method will be called from a bg thread, blocking IO operations are safe to use.</p>
     *
     * @param transactionProcess can be used to access both generated transaction and all of its associated
     *                           PendingPayments.
     *                           <p>Note - It's for the dev to decide whether to call TransactionProcess.send()
     *                           to send using the sdk.
     *                           with or without a whitelisted payload, or to send it to the blockchain by dev
     *                           server and just return back the transaction id</p>
     * @return the transaction identifier.
     * @throws Exception
     */
    TransactionId interceptTransactionSending(T transactionProcess) throws Exception;
}

