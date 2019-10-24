package kin.sdk.internal.services

import kin.base.Server
import kin.base.requests.RequestBuilder
import kin.sdk.exception.OperationFailedException
import java.io.IOException

interface GeneralBlockchainInfoRetriever {

    /**
     * Get the current minimum fee that the network charges per operation.
     * This value is expressed in stroops.
     *
     * **Note:** This method accesses the network, and should not be called on the android main thread.
     *
     * @return the minimum fee.
     */
    val minimumFeeSync: Long
}

internal class GeneralBlockchainInfoRetrieverImpl(private val server: Server) : GeneralBlockchainInfoRetriever {

    override val minimumFeeSync: Long
        @Throws(OperationFailedException::class)
        get() {
            try {
                val response = server.ledgers().order(RequestBuilder.Order.DESC).limit(1).execute()
                val records = response.records
                if (records.isNotEmpty()) {
                    return records.first().baseFee!!
                }
                throw OperationFailedException("Couldn't retrieve minimum fee data")
            } catch (e: IOException) {
                throw OperationFailedException(e)
            }
        }
}
