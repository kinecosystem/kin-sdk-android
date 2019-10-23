package kin.sdk.internal.utils;


import kin.base.Server;
import kin.sdk.internal.services.BlockchainEvents;
import kin.sdk.internal.services.BlockchainEventsImpl;

public class BlockchainEventsCreator {

    private final Server server;

    public BlockchainEventsCreator(Server server) {
        this.server = server;
    }

    public BlockchainEvents create(String accountId) {
        return new BlockchainEventsImpl(server, accountId);
    }
}
