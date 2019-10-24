package kin.sdk.internal.services.helpers

import kin.base.AccountLedgerEntryChange
import kin.base.Asset
import kin.base.KeyPair
import kin.base.MemoText
import kin.base.PaymentOperation
import kin.base.responses.AccountResponse
import kin.base.responses.TransactionResponse
import kin.sdk.internal.services.helpers.KinBaseToSDKModels.Companion.ASSET_TYPE_NATIVE
import kin.sdk.models.Balance
import kin.sdk.models.PaymentInfo
import kin.sdk.models.TransactionId
import java.math.BigDecimal

internal class KinBaseToSDKModels internal constructor() {

    companion object {
        const val ASSET_TYPE_NATIVE = "native"
    }
}

fun AccountResponse.kinBalance(): Balance? {
    balances.forEach { assetBalance ->
        if (assetBalance.asset.type.equals(KinBaseToSDKModels.ASSET_TYPE_NATIVE, ignoreCase = true)) {
            return Balance(BigDecimal(assetBalance.balance))
        }
    }
    return null
}

fun AccountLedgerEntryChange.toBalance(): Balance = Balance(BigDecimal(balance))

fun TransactionResponse.newBalanceForAccount(accountKeyPair: KeyPair): Balance? {
    ledgerChanges?.forEach { ledgerChange ->
        ledgerChange.ledgerEntryUpdates?.forEach { ledgerEntryUpdate ->
            if (ledgerEntryUpdate is AccountLedgerEntryChange) {
                ledgerEntryUpdate.account?.let { account ->
                    if (accountKeyPair.accountId == account.accountId) {
                        return ledgerEntryUpdate.toBalance()
                    }
                }
            }
        }
    }
    return null
}

fun Asset.isNative() = type.equals(ASSET_TYPE_NATIVE, ignoreCase = true)

fun TransactionResponse.asKinPaymentInfos(): List<PaymentInfo> =
    operations
        ?.filter { operation ->
            operation is PaymentOperation
                && operation.asset != null
                && operation.asset.type.equals(ASSET_TYPE_NATIVE, ignoreCase = true)
        }
        ?.map { it as PaymentOperation }
        ?.map { operation ->
            PaymentInfo(
                createdAt,
                operation.destination.accountId,
                if (operation.sourceAccount != null) operation.sourceAccount.accountId
                else sourceAccount.accountId,
                BigDecimal(operation.amount),
                TransactionId(hash),
                feePaid!!,
                if (memo is MemoText) (memo as MemoText).text
                else ""
            )
        }.orEmpty()
