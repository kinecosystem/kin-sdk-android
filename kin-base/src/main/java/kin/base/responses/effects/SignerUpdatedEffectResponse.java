package kin.base.responses.effects;

import kin.base.Server;

/**
 * Represents signed_updated effect response.
 * @see <a href="https://www.stellar.org/developers/horizon/reference/resources/effect.html" target="_blank">Effect documentation</a>
 * @see kin.base.requests.EffectsRequestBuilder
 * @see Server#effects()
 */
public class SignerUpdatedEffectResponse extends SignerEffectResponse {
  SignerUpdatedEffectResponse(Integer weight, String publicKey) {
    super(weight, publicKey);
  }
}
