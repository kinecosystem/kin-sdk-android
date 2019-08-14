package kin.sdk.internal.blockchain.events;


import kin.base.Server;

public class BlockchainEventsCreator {

    private final Server server;

    public BlockchainEventsCreator(Server server) {
        this.server = server;
    }

    public BlockchainEvents create(String accountId) {
        return new BlockchainEvents(server, accountId);
    }
}
