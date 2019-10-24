package kin.sdk.internal.utils

import kin.base.Server
import kin.sdk.internal.services.BlockchainEvents
import kin.sdk.internal.services.BlockchainEventsImpl

interface BlockchainEventsCreator {
    fun create(accountId: String): BlockchainEvents
}

internal class BlockchainEventsCreatorImpl(private val server: Server) : BlockchainEventsCreator {
    override fun create(accountId: String): BlockchainEvents {
        return BlockchainEventsImpl(server, accountId)
    }
}

