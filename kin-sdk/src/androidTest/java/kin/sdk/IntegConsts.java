package kin.sdk;


final class IntegConsts {

//    static final String TEST_NETWORK_URL = "http://10.0.2.2:8000";
//    static final String TEST_NETWORK_ID = "private testnet";
//    static final String URL_CREATE_ACCOUNT = "http://10.0.2.2:8001/?addr=";


    // temporary until blockchain team will update the blockchain-ops repo
    // todo need to change after block chain team will update.
    static final String TEST_NETWORK_URL = "http://10.4.59.1:8008";
    static final String TEST_NETWORK_ID = "Integration Test Network ; zulucrypto";
    static final String URL_CREATE_ACCOUNT = "http://10.4.59.1:8001?addr="; // friend-bot
    static final String URL_FUND = "http://10.4.59.1:3000/fund?account=%s&amount="; // faucet
}
