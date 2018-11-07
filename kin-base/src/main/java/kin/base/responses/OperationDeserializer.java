package kin.base.responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import kin.base.KeyPair;
import kin.base.responses.operations.ManageDataOperationResponse;
import kin.base.responses.operations.OperationResponse;
import kin.base.responses.operations.CreateAccountOperationResponse;
import kin.base.responses.operations.PaymentOperationResponse;
import kin.base.responses.operations.PathPaymentOperationResponse;
import kin.base.responses.operations.ManageOfferOperationResponse;
import kin.base.responses.operations.CreatePassiveOfferOperationResponse;
import kin.base.responses.operations.SetOptionsOperationResponse;
import kin.base.responses.operations.ChangeTrustOperationResponse;
import kin.base.responses.operations.AllowTrustOperationResponse;
import kin.base.responses.operations.AccountMergeOperationResponse;
import kin.base.responses.operations.InflationOperationResponse;

import java.lang.reflect.Type;

class OperationDeserializer implements JsonDeserializer<OperationResponse> {
  @Override
  public OperationResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    // Create new Gson object with adapters needed in Operation
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(KeyPair.class, new KeyPairTypeAdapter().nullSafe())
            .create();

    int type = json.getAsJsonObject().get("type_i").getAsInt();
    switch (type) {
      case 0:
        return gson.fromJson(json, CreateAccountOperationResponse.class);
      case 1:
        return gson.fromJson(json, PaymentOperationResponse.class);
      case 2:
        return gson.fromJson(json, PathPaymentOperationResponse.class);
      case 3:
        return gson.fromJson(json, ManageOfferOperationResponse.class);
      case 4:
        return gson.fromJson(json, CreatePassiveOfferOperationResponse.class);
      case 5:
        return gson.fromJson(json, SetOptionsOperationResponse.class);
      case 6:
        return gson.fromJson(json, ChangeTrustOperationResponse.class);
      case 7:
        return gson.fromJson(json, AllowTrustOperationResponse.class);
      case 8:
        return gson.fromJson(json, AccountMergeOperationResponse.class);
      case 9:
        return gson.fromJson(json, InflationOperationResponse.class);
      case 10:
        return gson.fromJson(json, ManageDataOperationResponse.class);
      default:
        throw new RuntimeException("Invalid operation type");
    }
  }
}
