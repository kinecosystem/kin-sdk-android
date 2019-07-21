package kin.sdk;


final class IntegConsts {

    static final String TEST_NETWORK_URL = BuildConfig.INTEG_TESTS_NETWORK_URL.isEmpty() ?
            Environment.TEST.getNetworkUrl() : BuildConfig.INTEG_TESTS_NETWORK_URL;
    static final String TEST_NETWORK_ID = BuildConfig.INTEG_TESTS_NETWORK_PASSPHRASE.isEmpty() ?
            Environment.TEST.getNetworkPassphrase() : BuildConfig.INTEG_TESTS_NETWORK_PASSPHRASE;
    static final String FRIENDBOT_URL = BuildConfig.INTEG_TESTS_NETWORK_FRIENDBOT.isEmpty() ?
            "https://friendbot-testnet.kininfrastructure.com" : BuildConfig.INTEG_TESTS_NETWORK_FRIENDBOT;
    static final String URL_CREATE_ACCOUNT = FRIENDBOT_URL + "?addr=%s&amount=%s";
    static final String URL_FUND = FRIENDBOT_URL + "/fund?addr=%s&amount="; // faucet
    static final String URL_WHITELISTING_SERVICE = (BuildConfig.INTEG_TESTS_NETWORK_FRIENDBOT.isEmpty() ?
            "http://34.239.111.38:3000" : BuildConfig.INTEG_TESTS_NETWORK_FRIENDBOT) + "/whitelist";
}
