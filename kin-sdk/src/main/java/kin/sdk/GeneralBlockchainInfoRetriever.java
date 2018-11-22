package kin.sdk;

import kin.sdk.exception.OperationFailedException;

interface GeneralBlockchainInfoRetriever {

    /**
     * Get the current minimum fee that the network charges per operation.
     * This value is expressed in stroops.
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @return the minimum fee.
     */
    long getMinimumFeeSync() throws OperationFailedException;

}
