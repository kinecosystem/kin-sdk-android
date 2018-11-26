package kin.sdk;


import kin.base.Server;

class BlockchainEventsCreator {

    private final Server server;

    BlockchainEventsCreator(Server server) {
        this.server = server;
    }

    BlockchainEvents create(String accountId) {
        return new BlockchainEvents(server, accountId);
    }
}
