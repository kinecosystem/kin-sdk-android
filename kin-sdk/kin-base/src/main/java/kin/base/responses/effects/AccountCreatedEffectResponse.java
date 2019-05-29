package kin.base.responses.effects;

import com.google.gson.annotations.SerializedName;
import kin.base.Server;

/**
 * Represents account_created effect response.
 * @see <a href="https://www.stellar.org/developers/horizon/reference/resources/effect.html" target="_blank">Effect documentation</a>
 * @see kin.base.requests.EffectsRequestBuilder
 * @see Server#effects()
 */
public class AccountCreatedEffectResponse extends EffectResponse {
  @SerializedName("starting_balance")
  protected final String startingBalance;

  AccountCreatedEffectResponse(String startingBalance) {
    this.startingBalance = startingBalance;
  }

  public String getStartingBalance() {
    return startingBalance;
  }
}
