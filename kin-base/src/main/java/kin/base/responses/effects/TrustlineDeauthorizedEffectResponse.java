package kin.base.responses.effects;

import kin.base.KeyPair;
import kin.base.Server;

/**
 * Represents trustline_deauthorized effect response.
 * @see <a href="https://www.stellar.org/developers/horizon/reference/resources/effect.html" target="_blank">Effect documentation</a>
 * @see kin.base.requests.EffectsRequestBuilder
 * @see Server#effects()
 */
public class TrustlineDeauthorizedEffectResponse extends TrustlineAuthorizationResponse {
  TrustlineDeauthorizedEffectResponse(KeyPair trustor, String assetType, String assetCode) {
    super(trustor, assetType, assetCode);
  }
}
