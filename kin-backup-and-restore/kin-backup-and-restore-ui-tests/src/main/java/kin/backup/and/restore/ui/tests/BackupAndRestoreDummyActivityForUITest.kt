package kin.backup.and.restore.ui.tests

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import kin.backupandrestore.BackupAndRestoreManager
import kin.backupandrestore.BackupCallback
import kin.backupandrestore.RestoreCallback
import kin.backupandrestore.exception.BackupAndRestoreException
import kin.sdk.Environment
import kin.sdk.KinAccount
import kin.sdk.KinClient

class BackupAndRestoreDummyActivityForUITest : AppCompatActivity(), View.OnClickListener{

    private val reqCodeBackup = 9000
    private val reqCodeRestore = 9001

    private var createNewAccount: Button? = null

    private var backupCurrentAccount: Button? = null
    private var restoreAccount: Button? = null
    private var restoredAccountPublicAddress: TextView? = null
    private var isBackupAccountSucceeded: TextView? = null
    private var backupAndRestoreManager: BackupAndRestoreManager? = null
    private var kinClient: KinClient? = null
    private var kinAccount: KinAccount? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_and_restore_dummy_test)

        createNewAccount = findViewById(R.id.create_new_account)
        backupCurrentAccount = findViewById(R.id.backup_current_account)
        restoreAccount = findViewById(R.id.restore_account)
        restoredAccountPublicAddress = findViewById(R.id.public_address_value)
        isBackupAccountSucceeded = findViewById(R.id.backup_status_value)

        createNewAccount?.setOnClickListener(this)
        backupCurrentAccount?.setOnClickListener(this)
        restoreAccount?.setOnClickListener(this)

        kinClient =  KinClient(this, Environment.TEST, "utst", "backup_and_restore_ui_test")
        kinAccount = kinClient?.addAccount()
        backupAndRestoreManager = BackupAndRestoreManager(this, reqCodeBackup, reqCodeRestore)
        registerToCallbacks()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.backup_current_account -> {
                backupAndRestoreManager?.backup(kinClient, kinAccount)
            }
            R.id.restore_account -> {
                backupAndRestoreManager?.restore(kinClient)
            }
            R.id.create_new_account -> {
                kinAccount = kinClient?.addAccount()
                restoredAccountPublicAddress?.text = kinAccount?.publicAddress
            }
        }
    }

    private fun registerToCallbacks() {
        backupAndRestoreManager?.registerBackupCallback(object : BackupCallback {
            override fun onSuccess() {
                isBackupAccountSucceeded?.text = "SUCCEEDED"
            }

            override fun onCancel() {
                isBackupAccountSucceeded?.text = "CANCELED"
            }

            override fun onFailure(throwable: BackupAndRestoreException) {
                isBackupAccountSucceeded?.text = "FAILED"
            }
        })

        backupAndRestoreManager?.registerRestoreCallback(object : RestoreCallback {
            override fun onSuccess(kinClient: KinClient?, kinAccount: KinAccount?) {
                this@BackupAndRestoreDummyActivityForUITest.kinClient = kinClient
                this@BackupAndRestoreDummyActivityForUITest.kinAccount = kinAccount
                if (kinAccount != null) {
                    restoredAccountPublicAddress?.text = kinAccount.publicAddress
                } else {
                    restoredAccountPublicAddress?.text = "FAILED"
                }
            }

            override fun onCancel() {
                restoredAccountPublicAddress?.text = "CANCEL"
            }

            override fun onFailure(throwable: BackupAndRestoreException) {
                restoredAccountPublicAddress?.text = "FAILED"
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == reqCodeBackup || requestCode == reqCodeRestore) {
            backupAndRestoreManager?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backupAndRestoreManager?.release()
    }

}
