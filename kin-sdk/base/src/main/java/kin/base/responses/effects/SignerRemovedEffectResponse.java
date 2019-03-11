package kin.base.responses.effects;

/**
 * Represents signer_removed effect response.
 */
public class SignerRemovedEffectResponse extends SignerEffectResponse {
  SignerRemovedEffectResponse(Integer weight, String publicKey) {
    super(weight, publicKey);
  }
}
