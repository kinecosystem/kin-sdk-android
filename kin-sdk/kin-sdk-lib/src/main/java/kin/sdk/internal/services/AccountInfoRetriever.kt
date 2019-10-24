package kin.sdk.internal.services

import kin.base.KeyPair
import kin.base.Server
import kin.base.responses.HttpResponseException
import kin.sdk.exception.AccountNotFoundException
import kin.sdk.exception.OperationFailedException
import kin.sdk.internal.services.helpers.kinBalance
import kin.sdk.models.AccountStatus
import kin.sdk.models.Balance
import java.io.IOException

interface AccountInfoRetriever {

    /**
     * Get balance for the specified account.
     *
     * @param accountId the account ID to check balance
     * @return the account [Balance]
     * @throws AccountNotFoundException if account not created yet
     * @throws OperationFailedException any other error
     */
    @Throws(OperationFailedException::class)
    fun getBalance(accountId: String): Balance

    @AccountStatus
    @Throws(OperationFailedException::class)
    fun getStatus(accountId: String): Int
}

internal class AccountInfoRetrieverImpl(private val server: Server) : AccountInfoRetriever {

    @Throws(OperationFailedException::class)
    override fun getBalance(accountId: String): Balance {
        try {
            val accountResponse =
                server.accounts().account(KeyPair.fromAccountId(accountId))
                    ?: throw OperationFailedException("can't retrieve data for account $accountId")
            return accountResponse.kinBalance() ?: throw OperationFailedException(accountId)
        } catch (httpError: HttpResponseException) {
            if (httpError.statusCode == 404) {
                throw AccountNotFoundException(accountId)
            } else {
                throw OperationFailedException(httpError)
            }
        } catch (e: IOException) {
            throw OperationFailedException(e)
        }
    }

    @AccountStatus
    @Throws(OperationFailedException::class)
    override fun getStatus(accountId: String): Int {
        try {
            getBalance(accountId)
            return AccountStatus.CREATED
        } catch (e: AccountNotFoundException) {
            return AccountStatus.NOT_CREATED
        }
    }
}
