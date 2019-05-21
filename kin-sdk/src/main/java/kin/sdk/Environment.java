package kin.sdk;

import static kin.sdk.Utils.checkNotEmpty;

import kin.base.Network;

/**
 * Provides blockchain network details
 */
public class Environment {

    public static final Environment PRODUCTION =
		new Environment("https://horizon.kinfederation.com",
			"Kin Mainnet ; December 2018");

    public static final Environment TEST =
        new Environment("https://horizon-testnet.kininfrastructure.com/",
            "Kin Testnet ; December 2018");

    private final String networkUrl;
    private final Network network;

    /**
     * Build an Environment object.
     * @param networkUrl the URL of the blockchain node.
     * @param networkPassphrase the network id to be used.
     */
    public Environment(String networkUrl, String networkPassphrase) {
        checkNotEmpty(networkUrl, "networkUrl");
        checkNotEmpty(networkPassphrase, "networkPassphrase");
        this.networkUrl = networkUrl;
        this.network = new Network(networkPassphrase);
    }

    /**
     * Returns the URL of the blockchain node.
     */
    @SuppressWarnings("WeakerAccess")
    final public String getNetworkUrl() {
        return networkUrl;
    }

    /**
     * Returns the network id.
     */
    @SuppressWarnings("WeakerAccess")
    final public String getNetworkPassphrase() {
        return network.getNetworkPassphrase();
    }


    final public boolean isMainNet() {
        return PRODUCTION.getNetworkPassphrase().equals(network.getNetworkPassphrase());
    }

    final Network getNetwork() {
        return network;
    }

}
