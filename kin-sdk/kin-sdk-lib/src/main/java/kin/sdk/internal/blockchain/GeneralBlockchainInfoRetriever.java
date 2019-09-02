package kin.sdk.internal.blockchain;

import kin.sdk.exception.OperationFailedException;

public interface GeneralBlockchainInfoRetriever {

    /**
     * Get the current minimum fee that the network charges per operation.
     * This value is expressed in Quarks (1 Quark = 0.00001 KIN).
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @return the minimum fee.
     */
    long getMinimumFeeSync() throws OperationFailedException;

}
