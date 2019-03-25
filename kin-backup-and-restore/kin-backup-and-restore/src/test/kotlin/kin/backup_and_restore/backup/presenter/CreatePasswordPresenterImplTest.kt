package kin.backup_and_restore.backup.presenter

import com.nhaarman.mockitokotlin2.*
import kin.backup_and_restore.backup.view.BackupNavigator
import kin.backup_and_restore.backup.view.CreatePasswordView
import kin.backup_and_restore.events.BackupEventCode.BACKUP_CREATE_PASSWORD_PAGE_VIEWED
import kin.backup_and_restore.events.CallbackManager
import kin.sdk.KinAccount
import kin.sdk.exception.CryptoException
import org.junit.Before
import org.junit.Test

class CreatePasswordPresenterImplTest {

    private val callbackManager: CallbackManager = mock()
    private val kinAccount: KinAccount = mock()
    private val backupNavigator: BackupNavigator = mock()

    private val view: CreatePasswordView = mock()

    private val pass = "1234qwerQ!"
    private val otherPass = "something_else"
    private val accountKey = "some_account_key"

    private lateinit var presenter: CreatePasswordPresenterImpl

    @Before
    fun setUp() {
        presenter = CreatePasswordPresenterImpl(callbackManager, backupNavigator, kinAccount)
        presenter.onAttach(view)
    }

    @Test
    fun `send create password page view event on create`() {
        verify(callbackManager).sendBackupEvent(BACKUP_CREATE_PASSWORD_PAGE_VIEWED)
    }

    @Test
    fun `onBackClicked and navigator has been notified`() {
        presenter.onBackClicked()
        verify(backupNavigator).closeFlow()
    }

    @Test
    fun `enter password changed, password is valid but did not complete all other requirements`() {
        presenter.enterPasswordChanged(pass, otherPass)
        verify(view).disableNextButton()
    }

    @Test
    fun `enter password changed, password is valid and completed all other requirements`() {
        presenter.apply {
            enterPasswordChanged(pass, "")
            confirmPasswordChanged(pass, pass)
            iUnderstandChecked(true)
        }
        verify(view).enableNextButton()
    }

    @Test
    fun `enter password changed, password is not empty but not valid`() {
        presenter.enterPasswordChanged(otherPass, pass)
        verify(view).setEnterPasswordIsCorrect(false)
        verify(view).disableNextButton()
    }

    @Test
    fun `enter password changed, password is empty reset fields`() {
        presenter.enterPasswordChanged("", otherPass)
        verify(view).resetEnterPasswordField()
        verify(view).resetConfirmPasswordField()
    }

    @Test
    fun `confirm password changed, some of the passwords is empty`() {
        presenter.confirmPasswordChanged("", otherPass)
        verify(view).resetConfirmPasswordField()
    }

    @Test
    fun `confirm password changed, password does not match`() {
        presenter.confirmPasswordChanged(pass, otherPass)
        verify(view).setConfirmPasswordIsCorrect(false)
    }

    @Test
    fun `confirm password changed, password matches`() {
        presenter.confirmPasswordChanged(pass, pass)
        verify(view).setConfirmPasswordIsCorrect(true)
        verify(view).closeKeyboard()
    }

    @Test
    fun `i understand is checked and passwords are matches, next button should become enabled`() {
        presenter.apply {
            enterPasswordChanged(pass, pass)
            confirmPasswordChanged(pass, pass)
            iUnderstandChecked(true)
        }
        verify(view).enableNextButton()
    }

    @Test
    fun `i understand is unchecked`() {
        presenter.apply {
            enterPasswordChanged(pass, pass)
            confirmPasswordChanged(pass, otherPass)
            iUnderstandChecked(false)
        }
        verify(view, times(3)).disableNextButton()
    }

    @Test
    fun `next button clicked and export succeeded`() {
        whenever(kinAccount.export(pass)) doReturn (accountKey)
        presenter.nextButtonClicked(pass)
        verify(backupNavigator).navigateToSaveAndSharePage(accountKey)
    }

    @Test
    fun `next button clicked and export failed`() {
        whenever(kinAccount.export(pass)) doThrow (CryptoException::class)
        presenter.nextButtonClicked(pass)
        verify(view).showBackupFailed()
    }

    @Test
    fun `onRetryClicked and export succeed`() {
        whenever(kinAccount.export(pass)) doReturn (accountKey)
        presenter.onRetryClicked(pass)
        verify(backupNavigator).navigateToSaveAndSharePage(accountKey)
    }

    @Test
    fun `onRetryClicked and export failed`() {
        whenever(kinAccount.export(pass)) doThrow (CryptoException::class)
        presenter.onRetryClicked(pass)
        verify(view).showBackupFailed()
    }

}