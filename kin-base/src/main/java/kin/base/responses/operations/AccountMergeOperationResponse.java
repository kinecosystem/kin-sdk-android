package kin.base.responses.operations;

import com.google.gson.annotations.SerializedName;

import kin.base.KeyPair;
import kin.base.Server;

/**
 * Represents AccountMerge operation response.
 * @see <a href="https://www.stellar.org/developers/horizon/reference/resources/operation.html" target="_blank">Operation documentation</a>
 * @see kin.base.requests.OperationsRequestBuilder
 * @see Server#operations()
 */
public class AccountMergeOperationResponse extends OperationResponse {
  @SerializedName("account")
  protected final KeyPair account;
  @SerializedName("into")
  protected final KeyPair into;

  AccountMergeOperationResponse(KeyPair account, KeyPair into) {
    this.account = account;
    this.into = into;
  }

  public KeyPair getAccount() {
    return account;
  }

  public KeyPair getInto() {
    return into;
  }
}
