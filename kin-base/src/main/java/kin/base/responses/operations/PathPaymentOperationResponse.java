package kin.base.responses.operations;

import com.google.gson.annotations.SerializedName;

import kin.base.Asset;
import kin.base.AssetTypeNative;
import kin.base.KeyPair;
import kin.base.Server;

/**
 * Represents PathPayment operation response.
 * @see <a href="https://www.stellar.org/developers/horizon/reference/resources/operation.html" target="_blank">Operation documentation</a>
 * @see kin.base.requests.OperationsRequestBuilder
 * @see Server#operations()
 */
public class PathPaymentOperationResponse extends OperationResponse {
  @SerializedName("amount")
  protected final String amount;
  @SerializedName("source_amount")
  protected final String sourceAmount;
  @SerializedName("from")
  protected final KeyPair from;
  @SerializedName("to")
  protected final KeyPair to;

  @SerializedName("asset_type")
  protected final String assetType;
  @SerializedName("asset_code")
  protected final String assetCode;
  @SerializedName("asset_issuer")
  protected final String assetIssuer;

  @SerializedName("send_asset_type")
  protected final String sendAssetType;
  @SerializedName("send_asset_code")
  protected final String sendAssetCode;
  @SerializedName("send_asset_issuer")
  protected final String sendAssetIssuer;

  PathPaymentOperationResponse(String amount, String sourceAmount, KeyPair from, KeyPair to, String assetType, String assetCode, String assetIssuer, String sendAssetType, String sendAssetCode, String sendAssetIssuer) {
    this.amount = amount;
    this.sourceAmount = sourceAmount;
    this.from = from;
    this.to = to;
    this.assetType = assetType;
    this.assetCode = assetCode;
    this.assetIssuer = assetIssuer;
    this.sendAssetType = sendAssetType;
    this.sendAssetCode = sendAssetCode;
    this.sendAssetIssuer = sendAssetIssuer;
  }

  public String getAmount() {
    return amount;
  }

  public String getSourceAmount() {
    return sourceAmount;
  }

  public KeyPair getFrom() {
    return from;
  }

  public KeyPair getTo() {
    return to;
  }

  public Asset getAsset() {
    if (assetType.equals("native")) {
      return new AssetTypeNative();
    } else {
      KeyPair issuer = KeyPair.fromAccountId(assetIssuer);
      return Asset.createNonNativeAsset(assetCode, issuer);
    }
  }

  public Asset getSendAsset() {
    if (sendAssetType.equals("native")) {
      return new AssetTypeNative();
    } else {
      KeyPair issuer = KeyPair.fromAccountId(sendAssetIssuer);
      return Asset.createNonNativeAsset(sendAssetCode, issuer);
    }
  }
}
