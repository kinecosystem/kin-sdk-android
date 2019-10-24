package kin.sdk.internal.services

import kin.base.KeyPair
import kin.base.Server
import kin.base.responses.TransactionResponse
import kin.sdk.internal.services.helpers.EventListener
import kin.sdk.internal.services.helpers.ListenerRegistration
import kin.sdk.internal.services.helpers.ManagedServerSentEventStream
import kin.sdk.internal.services.helpers.asKinPaymentInfos
import kin.sdk.internal.services.helpers.newBalanceForAccount
import kin.sdk.models.Balance
import kin.sdk.models.PaymentInfo

interface BlockchainEvents {

    /**
     * Creates and adds listener for balance changes of this account, use returned [ListenerRegistration] to
     * stop listening.
     *
     ***Note:** Events will be fired on background thread.
     *
     * @param listener listener object for payment events
     */
    fun addBalanceListener(listener: EventListener<Balance>): ListenerRegistration

    /**
     * Creates and adds listener for payments concerning this account, use returned [ListenerRegistration] to
     * stop listening.
     *
     ***Note:** Events will be fired on background thread.
     *
     * @param listener listener object for payment events
     */
    fun addPaymentListener(listener: EventListener<PaymentInfo>): ListenerRegistration

    /**
     * Creates and adds listener for account creation event, use returned [ListenerRegistration] to stop
     * listening.
     *
     ***Note:** Events will be fired on background thread.
     *
     * @param listener listener object for payment events
     */
    fun addAccountCreationListener(listener: EventListener<Void?>): ListenerRegistration
}

/**
 * Provides listeners, for various events happens on the blockchain.
 */
internal class BlockchainEventsImpl(server: Server, accountId: String) : BlockchainEvents {

    companion object {
        private const val CURSOR_FUTURE_ONLY = "now"
    }

    private val accountKeyPair: KeyPair = KeyPair.fromAccountId(accountId)
    private val transactionsStream: ManagedServerSentEventStream<TransactionResponse> =
        ManagedServerSentEventStream(
            server.transactions()
                .forAccount(this.accountKeyPair)
                .cursor(CURSOR_FUTURE_ONLY)
        )

    override fun addBalanceListener(listener: EventListener<Balance>): ListenerRegistration {
        return attachListener(kin.base.requests.EventListener { transactionResponse ->
            transactionResponse.newBalanceForAccount(accountKeyPair)?.let { listener.onEvent(it) }
        })
    }

    override fun addPaymentListener(listener: EventListener<PaymentInfo>): ListenerRegistration {
        return attachListener(kin.base.requests.EventListener { transactionResponse ->
            transactionResponse.asKinPaymentInfos()
                .forEach {
                    listener.onEvent(it)
                }
        })
    }

    override fun addAccountCreationListener(listener: EventListener<Void?>): ListenerRegistration {
        return attachListener(object : kin.base.requests.EventListener<TransactionResponse> {
            private var eventOccurred = false

            override fun onEvent(transactionResponse: TransactionResponse) {
                //account creation is one time operation, fire event only once
                if (!eventOccurred) {
                    eventOccurred = true
                    listener.onEvent(null)
                }
            }
        })
    }

    private fun attachListener(
        responseListener: kin.base.requests.EventListener<TransactionResponse>
    ): ListenerRegistration {
        transactionsStream.addListener(responseListener)
        return ListenerRegistration(Runnable { transactionsStream.removeListener(responseListener) })
    }
}
