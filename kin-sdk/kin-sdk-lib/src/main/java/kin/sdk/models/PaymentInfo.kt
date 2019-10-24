package kin.sdk.models

import java.math.BigDecimal

data class PaymentInfo(
    val createdAt: String,
    val destinationPublicKey: String,
    val sourcePublicKey: String,
    val amount: BigDecimal,
    val hash: TransactionId,
    val fee: Long,
    val memo: String?
)
