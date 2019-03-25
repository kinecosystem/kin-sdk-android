package kin.base;

import kin.base.xdr.OperationType;

/**
 * Represents <a href="https://www.stellar.org/developers/learn/concepts/list-of-operations.html#inflation" target="_blank">Inflation</a> operation.
 * @see <a href="https://www.stellar.org/developers/learn/concepts/list-of-operations.html" target="_blank">List of Operations</a>
 */
public class InflationOperation extends Operation {
    @Override
    kin.base.xdr.Operation.OperationBody toOperationBody() {
        kin.base.xdr.Operation.OperationBody body = new kin.base.xdr.Operation.OperationBody();
        body.setDiscriminant(OperationType.INFLATION);
        return body;
    }
}
