package kin.sdk

import kin.base.Network

/**
 * Provides blockchain network details
 */
class Environment
/**
 * Build an Environment object.
 *
 * @param networkUrl        the URL of the blockchain node.
 * @param networkPassphrase the network id to be used.
 */
(
    /**
     * Returns the URL of the blockchain node.
     */
    val networkUrl: String,
    networkPassphrase: String
) {
    companion object {

        val PRODUCTION = Environment("https://horizon.kinfederation.com",
            "Kin Mainnet ; December 2018")

        val TEST = Environment("https://horizon-testnet.kininfrastructure.com/",
            "Kin Testnet ; December 2018")

        private fun checkNotEmpty(string: String?, paramName: String) {
            require(!(string == null || string.isEmpty())) { "$paramName cannot be null or empty." }
        }
    }

    val network: Network

    /**
     * Returns the network id.
     */
    val networkPassphrase: String
        get() = network.networkPassphrase

    val isMainNet: Boolean
        get() = PRODUCTION.networkPassphrase == network.networkPassphrase

    init {
        checkNotEmpty(networkUrl, "networkUrl")
        checkNotEmpty(networkPassphrase, "networkPassphrase")
        this.network = Network(networkPassphrase)
    }
}
