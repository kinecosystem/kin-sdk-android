package kin.backup.and.restore.ui.tests

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.closeSoftKeyboard
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.UiDevice
import android.widget.TextView
import kin.backup.and.restore.ui.tests.UiTestUtils.childAtPosition
import kin.backup.and.restore.ui.tests.UiTestUtils.hasValueEqualTo
import kin.backup.and.restore.ui.tests.UiTestUtils.isSameDrawable
import kin.backupandrestore.backup.view.BackupActivity
import kin.sdk.Environment
import kin.sdk.KinClient
import org.hamcrest.Matchers.*
import org.hamcrest.core.IsInstanceOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals


@LargeTest
@RunWith(AndroidJUnit4::class)
class BackupActivityValidateSwitchBetweenScreens {

    private lateinit var kinClient: KinClient
    private lateinit var stubIntent: Intent
    private var componentName: ComponentName? = null

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(BackupActivity::class.java, false, false)


    @Before
    fun setup() {
        kinClient = KinClient(InstrumentationRegistry.getTargetContext(), Environment.TEST, UiTestUtils.appId, UiTestUtils.storeKey)
        kinClient.clearAllAccounts()
        val kinAccount = kinClient.addAccount()
        stubIntent = UiTestUtils.createStubIntent(BackupActivity::class.java.name, kinAccount.publicAddress)
        Intents.init()
        componentName = UiTestUtils.componentName
    }

    @After
    fun teardown() {
        if (::kinClient.isInitialized) {
            kinClient.clearAllAccounts()
        }
    }

    @Test
    fun backupActivity_FirstScreenToSecondScreen_CorrectComponents() {
        launchActivity()

        val backImageButton = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.toolbar),
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.view.ViewGroup::class.java),
                                        0)),
                        0),
                        isDisplayed()))
        backImageButton.check(matches(isDisplayed()))
                .check(matches(isSameDrawable(R.drawable.back)))

        val toolbarTitle = onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
        toolbarTitle.check(doesNotExist())

        val textView = onView(withId(R.id.steps_text))
        textView.check(matches(withText("")))
                .check(matches(not(isDisplayed())))

        val appCompatButton = onView(
                allOf(withId(R.id.lets_go_button), withText(R.string.backup_and_restore_lets_go),
                        isDisplayed()))
        appCompatButton.perform(click())

        closeSoftKeyboard()

        onView(allOf(instanceOf(TextView::class.java),
                withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.backup_and_restore_create_password)))

        val textView2 = onView(
                allOf(withId(R.id.steps_text), withText("1/2"),
                        isDisplayed()))
        textView2.check(matches(withText("1/2")))

        val targetContext = InstrumentationRegistry.getTargetContext()
        onView(withId(R.id.enter_pass_edittext)).check(matches(hasValueEqualTo(targetContext.resources.getString(R.string.backup_and_restore_enter_password))))
        onView(withId(R.id.confirm_pass_edittext)).check(matches(hasValueEqualTo(targetContext.resources.getString(R.string.backup_and_restore_confirm_password))))

        val button = onView(
                allOf(withId(R.id.next_button), not(isEnabled()), isDisplayed()))
        button.check(matches(isDisplayed()))
    }

    @Test
    fun backupActivity_SecondScreenToThirdScreen_CorrectComponents() {
        launchActivity()

        val letsGoButton = onView(allOf(withId(R.id.lets_go_button), withText(R.string.backup_and_restore_lets_go)))
        letsGoButton.perform(click())
        closeSoftKeyboard()
        val password = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.enter_pass_edittext),
                                childAtPosition(
                                        withClassName(`is`("android.support.constraint.ConstraintLayout")),
                                        2)),
                        0),
                        isDisplayed()))
        password.perform(replaceText("qwertyU1!"))

        val confirm = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.confirm_pass_edittext),
                                childAtPosition(
                                        withClassName(`is`("android.support.constraint.ConstraintLayout")),
                                        3)),
                        0),
                        isDisplayed()))
        confirm.perform(replaceText("qwertyU1!"))

        closeSoftKeyboard()

        val understandCheckbox = onView(withId(R.id.understand_checkbox))
        understandCheckbox.perform(click())

        val nextButton = onView(withId(R.id.next_button))
        nextButton.perform(click())

        val toolbarTitle = onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
        toolbarTitle.check(matches(withText(R.string.backup_and_restore_my_kin_wallet_qr_code)))

        val textView = onView(
                allOf(withId(R.id.steps_text), withText("2/2"),
                        isDisplayed()))
        textView.check(matches(withText("2/2")))

        val button = onView(
                allOf(withId(R.id.send_email_button), isEnabled(), isDisplayed()))
        button.check(matches(isDisplayed()))
                .check(matches(isEnabled()))

        val imageButton = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.toolbar),
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.view.ViewGroup::class.java),
                                        0)),
                        0),
                        isDisplayed()))
        imageButton.check(matches(isDisplayed()))
                .check(matches(isSameDrawable(R.drawable.back)))
    }

    @Test
    fun backupActivity_ThirdScreen_ToFourth_CorrectComponents() {
        launchActivity()


        val letsGoButton = onView(allOf(withId(R.id.lets_go_button), withText(R.string.backup_and_restore_lets_go)))
        letsGoButton.perform(click())

        val password = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.enter_pass_edittext),
                                childAtPosition(
                                        withClassName(`is`("android.support.constraint.ConstraintLayout")),
                                        2)),
                        0),
                        isDisplayed()))
        password.perform(replaceText("qwertyU1!"))

        val confirm = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.confirm_pass_edittext),
                                childAtPosition(
                                        withClassName(`is`("android.support.constraint.ConstraintLayout")),
                                        3)),
                        0),
                        isDisplayed()))
        confirm.perform(replaceText("qwertyU1!"))

        closeSoftKeyboard()

        val understandCheckbox = onView(withId(R.id.understand_checkbox))
        understandCheckbox.perform(click())

        val nextButton = onView(withId(R.id.next_button))
        nextButton.perform(click())

        val toolbarTitle = onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
        toolbarTitle.check(matches(withText(R.string.backup_and_restore_my_kin_wallet_qr_code)))

        val button = onView(
                allOf(withId(R.id.send_email_button), isEnabled(), isDisplayed()))
        button.perform(click())

        val device = UiDevice.getInstance(getInstrumentation())
        device.pressBack()

        val understandCheckbox2 = onView(withId(R.id.i_saved_my_qr_checkbox))
        understandCheckbox2.perform(click())

        val toolbarTitle2 = onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
        toolbarTitle2.check(doesNotExist())

        val textView = onView(withId(R.id.steps_text))
        textView.check(matches(withText("")))
                .check(matches(not(isDisplayed())))

        val imageButton = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.toolbar),
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.view.ViewGroup::class.java),
                                        0)),
                        0),
                        isDisplayed()))
        imageButton.check(matches(isDisplayed()))
                .check(matches(isSameDrawable(R.drawable.close)))

        val f = Activity::class.java.getDeclaredField("mResultCode")
        f.isAccessible = true
        val mResultCode = f.getInt(activityTestRule.activity)

        assertEquals(mResultCode, 5000) //5000 is success
    }

    //TODO For full coverage we still need to add method to check when pressing back from screen X then go back to Screen Y and everything is correct
    //TODO Also when putting wrong password/confirm and when QR code

    private fun launchActivity() {
        activityTestRule.launchActivity(stubIntent)

        // Check the intent is handled by the app
        val expectedIntent = hasComponent(componentName)
        intended(expectedIntent)
        Intents.release()
    }


}