package kin.base.responses;

import junit.framework.TestCase;
import org.junit.Test;
import kin.base.Asset;
import kin.base.AssetTypeCreditAlphaNum;

public class AssetDeserializerTest extends TestCase {
    @Test
    public void testDeserializeNative() {
        String json = "{\"asset_type\": \"native\"}";
        Asset asset = GsonSingleton.getInstance().fromJson(json, Asset.class);
        assertEquals(asset.getType(), "native");
    }

    @Test
    public void testDeserializeCredit() {
        String json = "{\n" +
                "  \"asset_type\": \"credit_alphanum4\",\n" +
                "  \"asset_code\": \"CNY\",\n" +
                "  \"asset_issuer\": \"GAREELUB43IRHWEASCFBLKHURCGMHE5IF6XSE7EXDLACYHGRHM43RFOX\"\n" +
                "}";
        Asset asset = GsonSingleton.getInstance().fromJson(json, Asset.class);
        assertEquals(asset.getType(), "credit_alphanum4");
        AssetTypeCreditAlphaNum creditAsset = (AssetTypeCreditAlphaNum) asset;
        assertEquals(creditAsset.getCode(), "CNY");
        assertEquals(creditAsset.getIssuer().getAccountId(), "GAREELUB43IRHWEASCFBLKHURCGMHE5IF6XSE7EXDLACYHGRHM43RFOX");
    }
}
