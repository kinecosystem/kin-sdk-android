package kin.sdk;


final class IntegConsts {
    static final String TEST_NETWORK_URL = Environment.TEST.getNetworkUrl();
    static final String TEST_NETWORK_ID = Environment.TEST.getNetworkPassphrase();
    static final String FRIENDBOT_URL = "https://friendbot-testnet.kininfrastructure.com";
    static final String URL_CREATE_ACCOUNT = FRIENDBOT_URL + "?addr=%s&amount=%d";
    static final String URL_FUND = FRIENDBOT_URL + "/fund?addr=%s&amount="; // faucet
    static final String URL_WHITELISTING_SERVICE = "http://34.239.111.38:3000/whitelist";
}
