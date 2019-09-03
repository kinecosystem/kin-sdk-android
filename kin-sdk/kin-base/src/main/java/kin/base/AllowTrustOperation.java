package kin.base;

import static kin.base.Util.checkNotNull;

import kin.base.xdr.AccountID;
import kin.base.xdr.AllowTrustOp;
import kin.base.xdr.AssetType;
import kin.base.xdr.OperationType;

/**
 * Represents <a href="https://www.stellar.org/developers/learn/concepts/list-of-operations.html#allow-trust" target="_blank">AllowTrust</a> operation.
 * @see <a href="https://www.stellar.org/developers/learn/concepts/list-of-operations.html" target="_blank">List of Operations</a>
 */
public class AllowTrustOperation extends Operation {

  private final KeyPair trustor;
  private final String assetCode;
  private final boolean authorize;

  private AllowTrustOperation(KeyPair trustor, String assetCode, boolean authorize) {
    this.trustor = checkNotNull(trustor, "trustor cannot be null");
    this.assetCode = checkNotNull(assetCode, "assetCode cannot be null");
    this.authorize = authorize;
  }

  /**
   * The account of the recipient of the trustline.
   */
  public KeyPair getTrustor() {
    return trustor;
  }

  /**
   * The asset of the trustline the source account is authorizing. For example, if a gateway wants to allow another account to hold its USD credit, the type is USD.
   */
  public String getAssetCode() {
    return assetCode;
  }

  /**
   * Flag indicating whether the trustline is authorized.
   */
  public boolean getAuthorize() {
    return authorize;
  }

  @Override
  kin.base.xdr.Operation.OperationBody toOperationBody() {
    AllowTrustOp op = new AllowTrustOp();

    // trustor
    AccountID trustor = new AccountID();
    trustor.setAccountID(this.trustor.getXdrPublicKey());
    op.setTrustor(trustor);
    // asset
    AllowTrustOp.AllowTrustOpAsset asset = new AllowTrustOp.AllowTrustOpAsset();
    if (assetCode.length() <= 4) {
      asset.setDiscriminant(AssetType.ASSET_TYPE_CREDIT_ALPHANUM4);
      asset.setAssetCode4(Util.paddedByteArray(assetCode, 4));
    } else {
      asset.setDiscriminant(AssetType.ASSET_TYPE_CREDIT_ALPHANUM12);
      asset.setAssetCode12(Util.paddedByteArray(assetCode, 12));
    }
    op.setAsset(asset);
    // authorize
    op.setAuthorize(authorize);

    kin.base.xdr.Operation.OperationBody body = new kin.base.xdr.Operation.OperationBody();
    body.setDiscriminant(OperationType.ALLOW_TRUST);
    body.setAllowTrustOp(op);
    return body;
  }

  /**
   * Builds AllowTrust operation.
   * @see AllowTrustOperation
   */
  public static class Builder {
    private final KeyPair trustor;
    private final String assetCode;
    private final boolean authorize;

    private KeyPair mSourceAccount;

    Builder(AllowTrustOp op) {
      trustor = KeyPair.fromXdrPublicKey(op.getTrustor().getAccountID());
      switch (op.getAsset().getDiscriminant()) {
        case ASSET_TYPE_CREDIT_ALPHANUM4:
          assetCode = new String(op.getAsset().getAssetCode4()).trim();
          break;
        case ASSET_TYPE_CREDIT_ALPHANUM12:
          assetCode = new String(op.getAsset().getAssetCode12()).trim();
          break;
        default:
          throw new RuntimeException("Unknown asset code");
      }
      authorize = op.getAuthorize();
    }

    /**
     * Creates a new AllowTrust builder.
     * @param trustor The account of the recipient of the trustline.
     * @param assetCode The asset of the trustline the source account is authorizing. For example, if a gateway wants to allow another account to hold its USD credit, the type is USD.
     * @param authorize Flag indicating whether the trustline is authorized.
     */
    public Builder(KeyPair trustor, String assetCode, boolean authorize) {
      this.trustor = trustor;
      this.assetCode = assetCode;
      this.authorize = authorize;
    }

    /**
     * Set source account of this operation
     * @param sourceAccount Source account
     * @return Builder object so you can chain methods.
     */
    public Builder setSourceAccount(KeyPair sourceAccount) {
      mSourceAccount = sourceAccount;
      return this;
    }

    /**
     * Builds an operation
     */
    public AllowTrustOperation build() {
      AllowTrustOperation operation = new AllowTrustOperation(trustor, assetCode, authorize);
      if (mSourceAccount != null) {
        operation.setSourceAccount(mSourceAccount);
      }
      return operation;
    }
  }
}
