package kin.backup.and.restore.ui.tests

import android.app.Activity
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.intent.Intents.intending
import android.support.test.espresso.intent.matcher.BundleMatchers
import android.support.test.espresso.intent.matcher.IntentMatchers.hasAction
import android.support.test.espresso.intent.matcher.IntentMatchers.hasExtras
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kin.backupandrestore.BackupAndRestoreManager
import kin.backupandrestore.qr.QRFileUriHandlerImpl
import kin.backupandrestore.widget.PasswordEditText
import kin.sdk.Environment
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher


object UiTestUtils {

    var componentName: ComponentName? = null
        private set
    const val appId = "utst"
    const val storeKey = "backup_and_restore_ui_test"
    private val networkUrl = Environment.TEST.networkUrl

    private val networkPassphrase = Environment.TEST.networkPassphrase
    private lateinit var intent: Intent

    fun createStubIntent(component: String, publicAddress: String? = null): Intent {
        // Setup the trigger Intent
        intent = Intent()
        intent.putExtra(BackupAndRestoreManager.NETWORK_URL_EXTRA, networkUrl)
        intent.putExtra(BackupAndRestoreManager.NETWORK_PASSPHRASE_EXTRA, networkPassphrase)
        intent.putExtra(BackupAndRestoreManager.APP_ID_EXTRA, appId)
        intent.putExtra(BackupAndRestoreManager.STORE_KEY_EXTRA, storeKey)
        if (publicAddress != null) {
            intent.putExtra(BackupAndRestoreManager.PUBLIC_ADDRESS_EXTRA, publicAddress)
        }

        // If for some reason there maybe more than one app that will handle this requirement.
        // Need to set the Intent explicit here, because it may have more than one app to handle
        // this action and we don't want to have the App chooser here
        val packageName = InstrumentationRegistry.getTargetContext().packageName
        componentName = ComponentName(packageName, component)
        intent.component = componentName

        return intent
    }

    fun intendingStubQRIntent(): Matcher<Intent>? {
        val intent: Intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("image/*")
        val title = "Choose QR image…"

        val bitmap = loadBitmapFromResource(this.javaClass, "qr_test.png")
        val uri = QRFileUriHandlerImpl(InstrumentationRegistry.getTargetContext()).saveFile(bitmap)

        val resultData = Intent.createChooser(intent, title)
        resultData.data = uri
        val result = Instrumentation.ActivityResult(
                Activity.RESULT_OK, resultData)

        val intentChooser = Intent.createChooser(intent, title, null)
        val originalIntent = intentChooser?.extras?.get(Intent.EXTRA_INTENT) as Intent

        val expectedIntent = allOf(hasAction(intentChooser.action), hasExtras(BundleMatchers.hasValue(
                object : BaseMatcher<Intent>() {

                    override fun describeTo(description: Description) {

                    }

                    override fun matches(item: Any): Boolean {
                        return (item as? Intent)?.filterEquals(originalIntent) ?: false
                    }
                })))

        intending(expectedIntent).respondWith(result)
        return expectedIntent
    }

    fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    fun hasValueEqualTo(content: String): Matcher<View> {

        return object : TypeSafeMatcher<View>() {

            override fun describeTo(description: Description) {
                description.appendText("Has EditText the value:  $content")
            }

            override fun matchesSafely(view: View?): Boolean {
                if (view !is PasswordEditText) {
                    return false
                }
                val text = view.hint
                return text.equals(content, ignoreCase = true)
            }
        }
    }

    fun isSameDrawable(expectedId: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {

            private var resourceName: String? = null

            override fun matchesSafely(target: View): Boolean {
                if (target !is ImageView) {
                    return false
                }
                if (expectedId < 0) {
                    return target.drawable == null
                }
                val resources = InstrumentationRegistry.getTargetContext().resources
                val expectedDrawable = resources.getDrawable(expectedId, null) ?: return false
                resourceName = resources.getResourceEntryName(expectedId)
                val bitmap = getBitmap(target.drawable)
                val otherBitmap = getBitmap(expectedDrawable)
                return bitmap.sameAs(otherBitmap)
            }

            override fun describeTo(description: Description) {
                description.appendText("with drawable from resource id: ")
                description.appendValue(expectedId)
                if (resourceName != null) {
                    description.appendText("[")
                    description.appendText(resourceName)
                    description.appendText("]")
                }
            }
        }
    }

    fun isTransparentDrawable(expectedId: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {

            private var resourceName: String? = null

            override fun matchesSafely(target: View): Boolean {
                if (target !is ImageView) {
                    return false
                }
                if (expectedId < 0) {
                    return target.drawable == null
                }
                return target.drawable.alpha == 0
            }

            override fun describeTo(description: Description) {
                description.appendText("with drawable from resource id: ")
                description.appendValue(expectedId)
                if (resourceName != null) {
                    description.appendText("[")
                    description.appendText(resourceName)
                    description.appendText("]")
                }
            }

        }
    }

    private fun getBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun loadBitmapFromResource(clazz: Class<*>, res: String): Bitmap {
        val inputStream = clazz.classLoader.getResourceAsStream(res)
        return BitmapFactory.decodeStream(inputStream)
    }

}