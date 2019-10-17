package kin.backupandrestore;

import android.support.annotation.Nullable;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;
import kin.sdk.Utils;

public class AccountExtractor {

    @Nullable
    public static KinAccount getKinAccount(KinClient kinClient, String publicAddress) {
        KinAccount kinAccount = null;
        if (kinClient != null && !Utils.isEmpty(publicAddress)) {
            int numOfAccounts = kinClient.getAccountCount();
            for (int i = 0; i < numOfAccounts; i++) {
                KinAccount account = kinClient.getAccount(i);
                if (account != null && account.getPublicAddress().equals(publicAddress)) {
                    kinAccount = account;
                    break;
                }
            }
        }
        return kinAccount;
    }
}
