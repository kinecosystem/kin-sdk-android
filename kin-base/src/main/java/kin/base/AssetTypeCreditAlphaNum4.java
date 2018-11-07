package kin.base;

import kin.base.xdr.AccountID;
import kin.base.xdr.AssetType;

/**
 * Represents all assets with codes 1-4 characters long.
 * @see <a href="https://www.stellar.org/developers/learn/concepts/assets.html" target="_blank">Assets</a>
 */
public final class AssetTypeCreditAlphaNum4 extends AssetTypeCreditAlphaNum {

  /**
   * Class constructor
   * @param code Asset code
   * @param issuer Asset issuer
   */
  public AssetTypeCreditAlphaNum4(String code, KeyPair issuer) {
    super(code, issuer);
    if (code.length() < 1 || code.length() > 4) {
      throw new AssetCodeLengthInvalidException();
    }
  }

  @Override
  public String getType() {
    return "credit_alphanum4";
  }

  @Override
  public kin.base.xdr.Asset toXdr() {
    kin.base.xdr.Asset xdr = new kin.base.xdr.Asset();
    xdr.setDiscriminant(AssetType.ASSET_TYPE_CREDIT_ALPHANUM4);
    kin.base.xdr.Asset.AssetAlphaNum4 credit = new kin.base.xdr.Asset.AssetAlphaNum4();
    credit.setAssetCode(Util.paddedByteArray(mCode, 4));
    AccountID accountID = new AccountID();
    accountID.setAccountID(mIssuer.getXdrPublicKey());
    credit.setIssuer(accountID);
    xdr.setAlphaNum4(credit);
    return xdr;
  }
}
