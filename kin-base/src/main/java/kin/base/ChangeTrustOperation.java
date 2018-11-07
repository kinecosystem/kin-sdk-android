package kin.base;

import static kin.base.Util.checkNotNull;

import kin.base.xdr.ChangeTrustOp;
import kin.base.xdr.Int64;
import kin.base.xdr.OperationType;

/**
 * Represents <a href="https://www.stellar.org/developers/learn/concepts/list-of-operations.html#change-trust" target="_blank">ChangeTrust</a> operation.
 * @see <a href="https://www.stellar.org/developers/learn/concepts/list-of-operations.html" target="_blank">List of Operations</a>
 */
public class ChangeTrustOperation extends Operation {

  private final Asset asset;
  private final String limit;

  private ChangeTrustOperation(Asset asset, String limit) {
    this.asset = checkNotNull(asset, "asset cannot be null");
    this.limit = checkNotNull(limit, "limit cannot be null");
  }

  /**
   * The asset of the trustline. For example, if a gateway extends a trustline of up to 200 USD to a user, the line is USD.
   */
  public Asset getAsset() {
    return asset;
  }

  /**
   * The limit of the trustline. For example, if a gateway extends a trustline of up to 200 USD to a user, the limit is 200.
   */
  public String getLimit() {
    return limit;
  }

  @Override
  kin.base.xdr.Operation.OperationBody toOperationBody() {
    ChangeTrustOp op = new ChangeTrustOp();
    op.setLine(asset.toXdr());
    Int64 limit = new Int64();
    limit.setInt64(Operation.toXdrAmount(this.limit));
    op.setLimit(limit);

    kin.base.xdr.Operation.OperationBody body = new kin.base.xdr.Operation.OperationBody();
    body.setDiscriminant(OperationType.CHANGE_TRUST);
    body.setChangeTrustOp(op);
    return body;
  }

  /**
   * Builds ChangeTrust operation.
   * @see ChangeTrustOperation
   */
  public static class Builder {
    private final Asset asset;
    private final String limit;

    private KeyPair mSourceAccount;

    Builder(ChangeTrustOp op) {
      asset = Asset.fromXdr(op.getLine());
      limit = Operation.fromXdrAmount(op.getLimit().getInt64().longValue());
    }

    /**
     * Creates a new ChangeTrust builder.
     * @param asset The asset of the trustline. For example, if a gateway extends a trustline of up to 200 USD to a user, the line is USD.
     * @param limit The limit of the trustline. For example, if a gateway extends a trustline of up to 200 USD to a user, the limit is 200.
     * @throws ArithmeticException when limit has more than 7 decimal places.
     */
    public Builder(Asset asset, String limit) {
      this.asset = checkNotNull(asset, "asset cannot be null");
      this.limit = checkNotNull(limit, "limit cannot be null");
    }

    /**
     * Set source account of this operation
     * @param sourceAccount Source account
     * @return Builder object so you can chain methods.
     */
    public Builder setSourceAccount(KeyPair sourceAccount) {
      mSourceAccount = checkNotNull(sourceAccount, "sourceAccount cannot be null");
      return this;
    }

    /**
     * Builds an operation
     */
    public ChangeTrustOperation build() {
      ChangeTrustOperation operation = new ChangeTrustOperation(asset, limit);
      if (mSourceAccount != null) {
        operation.setSourceAccount(mSourceAccount);
      }
      return operation;
    }
  }
}
