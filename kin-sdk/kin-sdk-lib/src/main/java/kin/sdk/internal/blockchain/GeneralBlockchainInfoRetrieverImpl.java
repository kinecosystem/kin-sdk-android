package kin.sdk.internal.blockchain;

import java.io.IOException;
import java.util.ArrayList;

import kin.base.Server;
import kin.base.requests.LedgersRequestBuilder;
import kin.base.requests.RequestBuilder;
import kin.base.responses.LedgerResponse;
import kin.base.responses.Page;
import kin.sdk.exception.OperationFailedException;

public class GeneralBlockchainInfoRetrieverImpl implements GeneralBlockchainInfoRetriever {

    private static final String ERROR_MESSAGE = "Couldn't retrieve minimum fee data";

    private final Server server;

    public GeneralBlockchainInfoRetrieverImpl(Server server) {
        this.server = server;
    }

    @Override
    public long getMinimumFeeSync() throws OperationFailedException {
        LedgersRequestBuilder builder = server.ledgers().order(RequestBuilder.Order.DESC).limit(1);

        try {
            Page<LedgerResponse> response = builder.execute();
            ArrayList<LedgerResponse> records = response.getRecords();
            if (records != null && !records.isEmpty()) {
                LedgerResponse ledgerResponse = records.get(0);
                if (ledgerResponse != null) {
                    return ledgerResponse.getBaseFee();
                }
            }
            throw new OperationFailedException("Couldn't retrieve minimum fee data");
        } catch (IOException e) {
            throw new OperationFailedException(ERROR_MESSAGE, e);
        }
    }
}
