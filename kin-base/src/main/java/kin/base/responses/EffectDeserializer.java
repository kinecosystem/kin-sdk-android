package kin.base.responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import kin.base.KeyPair;
import kin.base.responses.effects.AccountCreatedEffectResponse;
import kin.base.responses.effects.AccountCreditedEffectResponse;
import kin.base.responses.effects.AccountDebitedEffectResponse;
import kin.base.responses.effects.AccountFlagsUpdatedEffectResponse;
import kin.base.responses.effects.AccountHomeDomainUpdatedEffectResponse;
import kin.base.responses.effects.AccountRemovedEffectResponse;
import kin.base.responses.effects.AccountThresholdsUpdatedEffectResponse;
import kin.base.responses.effects.EffectResponse;
import kin.base.responses.effects.OfferCreatedEffectResponse;
import kin.base.responses.effects.OfferRemovedEffectResponse;
import kin.base.responses.effects.OfferUpdatedEffectResponse;
import kin.base.responses.effects.SignerCreatedEffectResponse;
import kin.base.responses.effects.SignerRemovedEffectResponse;
import kin.base.responses.effects.SignerUpdatedEffectResponse;
import kin.base.responses.effects.TradeEffectResponse;
import kin.base.responses.effects.TrustlineAuthorizedEffectResponse;
import kin.base.responses.effects.TrustlineCreatedEffectResponse;
import kin.base.responses.effects.TrustlineDeauthorizedEffectResponse;
import kin.base.responses.effects.TrustlineRemovedEffectResponse;
import kin.base.responses.effects.TrustlineUpdatedEffectResponse;

class EffectDeserializer implements JsonDeserializer<EffectResponse> {
  @Override
  public EffectResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    // Create new Gson object with adapters needed in Operation
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(KeyPair.class, new KeyPairTypeAdapter().nullSafe())
            .create();

    int type = json.getAsJsonObject().get("type_i").getAsInt();
    switch (type) {
      // Account effects
      case 0:
        return gson.fromJson(json, AccountCreatedEffectResponse.class);
      case 1:
        return gson.fromJson(json, AccountRemovedEffectResponse.class);
      case 2:
        return gson.fromJson(json, AccountCreditedEffectResponse.class);
      case 3:
        return gson.fromJson(json, AccountDebitedEffectResponse.class);
      case 4:
        return gson.fromJson(json, AccountThresholdsUpdatedEffectResponse.class);
      case 5:
        return gson.fromJson(json, AccountHomeDomainUpdatedEffectResponse.class);
      case 6:
        return gson.fromJson(json, AccountFlagsUpdatedEffectResponse.class);
      // Signer effects
      case 10:
        return gson.fromJson(json, SignerCreatedEffectResponse.class);
      case 11:
        return gson.fromJson(json, SignerRemovedEffectResponse.class);
      case 12:
        return gson.fromJson(json, SignerUpdatedEffectResponse.class);
      // Trustline effects
      case 20:
        return gson.fromJson(json, TrustlineCreatedEffectResponse.class);
      case 21:
        return gson.fromJson(json, TrustlineRemovedEffectResponse.class);
      case 22:
        return gson.fromJson(json, TrustlineUpdatedEffectResponse.class);
      case 23:
        return gson.fromJson(json, TrustlineAuthorizedEffectResponse.class);
      case 24:
        return gson.fromJson(json, TrustlineDeauthorizedEffectResponse.class);
      // Trading effects
      case 30:
        return gson.fromJson(json, OfferCreatedEffectResponse.class);
      case 31:
        return gson.fromJson(json, OfferRemovedEffectResponse.class);
      case 32:
        return gson.fromJson(json, OfferUpdatedEffectResponse.class);
      case 33:
        return gson.fromJson(json, TradeEffectResponse.class);
      default:
        throw new RuntimeException("Invalid operation type");
    }
  }
}
