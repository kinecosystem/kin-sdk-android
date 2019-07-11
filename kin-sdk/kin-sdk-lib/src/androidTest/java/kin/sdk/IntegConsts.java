package kin.sdk;


final class IntegConsts {

    static final String TEST_NETWORK_URL = BuildConfig.INTEG_TESTS_NETWORK_URL;
    static final String TEST_NETWORK_ID = BuildConfig.INTEG_TESTS_NETWORK_PASSPHRASE;
    static final String URL_CREATE_ACCOUNT = BuildConfig.INTEG_TESTS_NETWORK_FRIENDBOT + "?addr=%s&amount=%d";
    static final String URL_FUND = BuildConfig.INTEG_TESTS_NETWORK_FRIENDBOT + "/fund?addr=%s&amount="; // faucet

}
