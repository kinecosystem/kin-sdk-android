package kin.recovery.backup.presenter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kin.recovery.backup.view.BackupInfoView
import kin.recovery.backup.view.BackupNavigator
import kin.recovery.events.BackupEventCode.BACKUP_WELCOME_PAGE_START_TAPPED
import kin.recovery.events.BackupEventCode.BACKUP_WELCOME_PAGE_VIEWED
import kin.recovery.events.CallbackManager
import org.junit.Before
import org.junit.Test

class BackupInfoPresenterImplTest {

    private val callbackManager: CallbackManager = mock()
    private val backupNavigator: BackupNavigator = mock()

    private val view: BackupInfoView = mock()

    private lateinit var presenter: BackupInfoPresenterImpl

    @Before
    fun setUp() {
        presenter = BackupInfoPresenterImpl(callbackManager, backupNavigator)
        presenter.onAttach(view)
    }

    @Test
    fun `send welcome page view event`() {
        verify(callbackManager).sendBackupEvent(BACKUP_WELCOME_PAGE_VIEWED)
    }

    @Test
    fun `lets go clicked, navigate to create password page`() {
        presenter.letsGoButtonClicked()
        verify(callbackManager).sendBackupEvent(BACKUP_WELCOME_PAGE_START_TAPPED)
        verify(backupNavigator).navigateToCreatePasswordPage()
    }

    @Test
    fun `onBackClicked, close flow`() {
        presenter.onBackClicked()
        verify(backupNavigator).closeFlow()
    }
}