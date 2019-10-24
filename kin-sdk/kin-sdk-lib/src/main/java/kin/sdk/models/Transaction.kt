package kin.sdk.models

import kin.base.KeyPair
import java.math.BigDecimal

data class Transaction(
    val destination: KeyPair,
    val source: KeyPair,
    val amount: BigDecimal,
    val fee: Int,
    val memo: String?,
    /**
     * The transaction hash
     */
    val id: TransactionId,
    val stellarTransaction: kin.base.Transaction,
    val whitelistableTransaction: WhitelistableTransaction
)
