package kin.backupandrestore;

import android.support.annotation.Nullable;

import kin.sdk.IKinClient;
import kin.sdk.KinAccount;

public class AccountExtractor {

    @Nullable
    public static KinAccount getKinAccount(IKinClient kinClient, String publicAddress) {
        KinAccount kinAccount = null;
        if (kinClient != null && !isEmpty(publicAddress)) {
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

    private static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
}
