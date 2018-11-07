package kin.base.responses.operations;

import com.google.gson.annotations.SerializedName;

import kin.base.Asset;
import kin.base.AssetTypeNative;
import kin.base.KeyPair;
import kin.base.Server;

/**
 * Represents Payment operation response.
 *
 * @see <a href="https://www.stellar.org/developers/horizon/reference/resources/operation.html" target="_blank">Operation documentation</a>
 * @see kin.base.requests.OperationsRequestBuilder
 * @see Server#operations()
 */
public class PaymentOperationResponse extends OperationResponse {
  @SerializedName("created_at")
  protected final String createdAt;
  @SerializedName("transaction_hash")
  protected final String transactionHash;
  @SerializedName("amount")
  protected final String amount;
  @SerializedName("asset_type")
  protected final String assetType;
  @SerializedName("asset_code")
  protected final String assetCode;
  @SerializedName("asset_issuer")
  protected final String assetIssuer;
  @SerializedName("from")
  protected final KeyPair from;
  @SerializedName("to")
  protected final KeyPair to;

  PaymentOperationResponse(String createdAt, String transactionHash, String amount, String assetType, String assetCode, String assetIssuer, KeyPair from, KeyPair to) {
    this.createdAt = createdAt;
    this.transactionHash = transactionHash;
    this.amount = amount;
    this.assetType = assetType;
    this.assetCode = assetCode;
    this.assetIssuer = assetIssuer;
    this.from = from;
    this.to = to;
  }

  public String getAmount() {
    return amount;
  }

  public Asset getAsset() {
    if (assetType.equals("native")) {
      return new AssetTypeNative();
    } else {
      KeyPair issuer = KeyPair.fromAccountId(assetIssuer);
      return Asset.createNonNativeAsset(assetCode, issuer);
    }
  }

  public KeyPair getFrom() {
    return from;
  }

  public KeyPair getTo() {
    return to;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getTransactionHash() {
    return transactionHash;
  }
}
