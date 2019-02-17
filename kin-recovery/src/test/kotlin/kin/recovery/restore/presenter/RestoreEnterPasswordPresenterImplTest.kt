package kin.recovery.restore.presenter

import android.os.Bundle
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kin.recovery.KeyStoreProvider
import kin.recovery.events.CallbackManager
import kin.recovery.events.RestoreEventCode.*
import kin.recovery.exception.BackupException
import kin.recovery.exception.BackupException.CODE_RESTORE_INVALID_KEYSTORE_FORMAT
import kin.recovery.restore.presenter.RestorePresenterImpl.KEY_ACCOUNT_KEY
import kin.recovery.restore.view.RestoreEnterPasswordView
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class RestoreEnterPasswordPresenterImplTest {
    companion object {
        const val PASS = "pass"
        const val keyStoreData = "key store data"
    }

    private val callbackManager: CallbackManager = mock()
    private val keyStoreProvider: KeyStoreProvider = mock()
    private val view: RestoreEnterPasswordView = mock()
    private val parentPresenter: RestorePresenter = mock()

    private lateinit var presenter: RestoreEnterPasswordPresenterImpl

    @Before
    fun setUp() {
        createPresenter()
    }

    @Test
    fun `send event page viewed on create object`() {
        verify(callbackManager).sendRestoreEvent(RESTORE_PASSWORD_ENTRY_PAGE_VIEWED)
    }

    @Test
    fun `password changed, now its empty should disable done button`() {
        presenter.onPasswordChanged("")
        verify(view).disableDoneButton()
    }

    @Test
    fun `password changed, not empty should enable done button`() {
        presenter.onPasswordChanged(PASS)
        verify(view).enableDoneButton()
    }

    @Test
    fun `restore clicked, send event password done tapped`() {
        presenter.restoreClicked(PASS)
        verify(callbackManager).sendRestoreEvent(RESTORE_PASSWORD_DONE_TAPPED)
    }

    @Test
    fun `restore clicked import account succeed, navigate to complete page`() {
        val accountIndex = 1
        whenever(keyStoreProvider.importAccount(any(), any())).thenReturn(accountIndex)
        presenter.restoreClicked(PASS)
        verify(parentPresenter).navigateToRestoreCompletedPage(accountIndex);
    }

    @Test
    fun `restore clicked, exception with error code CODE_RESTORE_INVALID_KEYSTORE_FORMAT, show invalid qr error`() {
        whenever(keyStoreProvider.importAccount(any(), any())).thenThrow(BackupException(CODE_RESTORE_INVALID_KEYSTORE_FORMAT, "some msg"))
        presenter.restoreClicked(PASS)
        verify(view).invalidQrError()
    }

    @Test
    fun `restore clicked, exception with decode error , show decode error`() {
        whenever(keyStoreProvider.importAccount(any(), any())).thenThrow(BackupException::class.java)
        presenter.restoreClicked(PASS)
        verify(view).decodeError()
    }

    @Test
    fun `onSaveInstanceState save the the correct data`() {
        val bundle = Bundle()
        presenter.onSaveInstanceState(bundle)
        assertEquals(keyStoreData, bundle.getString(KEY_ACCOUNT_KEY))
    }

    @Test
    fun `back clicked send event and go to previous step`() {
        presenter.onBackClicked()
        verify(callbackManager).sendRestoreEvent(RESTORE_PASSWORD_ENTRY_PAGE_BACK_TAPPED)
        verify(parentPresenter).previousStep()
    }

    private fun createPresenter() {
        presenter = RestoreEnterPasswordPresenterImpl(callbackManager, keyStoreData, keyStoreProvider)
        presenter.onAttach(view, parentPresenter)
    }
}