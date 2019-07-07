//package kin.backup.and.restore.ui.tests
//
//import android.app.Activity
//import android.content.ComponentName
//import android.content.Intent
//import android.support.test.InstrumentationRegistry
//import android.support.test.espresso.Espresso.onView
//import android.support.test.espresso.action.ViewActions.*
//import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
//import android.support.test.espresso.assertion.ViewAssertions.matches
//import android.support.test.espresso.intent.Intents
//import android.support.test.espresso.intent.Intents.intended
//import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
//import android.support.test.espresso.matcher.ViewMatchers.*
//import android.support.test.filters.LargeTest
//import android.support.test.rule.ActivityTestRule
//import android.support.test.runner.AndroidJUnit4
//import android.widget.TextView
//import kin.backup.and.restore.ui.tests.UiTestUtils.childAtPosition
//import kin.backup.and.restore.ui.tests.UiTestUtils.hasValueEqualTo
//import kin.backup.and.restore.ui.tests.UiTestUtils.intendingStubQRIntent
//import kin.backup.and.restore.ui.tests.UiTestUtils.isSameDrawable
//import kin.backup.and.restore.ui.tests.UiTestUtils.isTransparentDrawable
//import kin.backupandrestore.restore.view.RestoreActivity
//import kin.sdk.Environment
//import kin.sdk.KinClient
//import org.hamcrest.Matcher
//import org.hamcrest.Matchers.*
//import org.hamcrest.core.IsInstanceOf
//import org.junit.After
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import kotlin.test.assertEquals
//
//@LargeTest
//@RunWith(AndroidJUnit4::class)
//class RestoreActivityValidateSwitchBetweenScreens {
//
//    private lateinit var kinClient: KinClient
//    private lateinit var stubIntent: Intent
//    private var componentName: ComponentName? = null
//    private var expectedIntent: Matcher<Intent>? = null
//
//    @get: Rule
//    var activityTestRule = ActivityTestRule(RestoreActivity::class.java, false, false)
//
//    @Before
//    fun setup() {
//        kinClient = KinClient(InstrumentationRegistry.getTargetContext(), Environment.TEST, UiTestUtils.appId, UiTestUtils.storeKey)
//        kinClient.clearAllAccounts()
//        stubIntent = UiTestUtils.createStubIntent(RestoreActivity::class.java.name)
//        componentName = UiTestUtils.componentName
//    }
//
//    @After
//    fun teardown() {
//        if (::kinClient.isInitialized) {
//            kinClient.clearAllAccounts()
//        }
//    }
//
//    @Test
//    fun restoreActivity_UploadAnDialogScreen_CorrectComponents() {
//        launchActivity()
//
//        Intents.init()
//        expectedIntent = intendingStubQRIntent()
//
//        val backImageButton = onView(
//                allOf(childAtPosition(
//                        allOf(withId(R.id.toolbar),
//                                childAtPosition(
//                                        IsInstanceOf.instanceOf(android.view.ViewGroup::class.java),
//                                        0)),
//                        0),
//                        isDisplayed()))
//        backImageButton.check(matches(isDisplayed()))
//                .check(matches(isSameDrawable(R.drawable.back)))
//
//        val toolbarTitle = onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
//        toolbarTitle.check(doesNotExist())
//
//        val textView = onView(withId(R.id.steps_text))
//        textView.check(matches(withText("")))
//                .check(matches(not(isDisplayed())))
//
//        val appCompatTextView = onView(
//                allOf(withId(R.id.upload_btn_text), withText(R.string.backup_and_restore_upload_qr_btn),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.fragment_frame),
//                                        0),
//                                5),
//                        isDisplayed()))
//        appCompatTextView.perform(click())
//
//        val appCompatButton2 = onView(
//                allOf(withId(android.R.id.button1), withText("OK"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.buttonPanel),
//                                        0),
//                                3)))
//        appCompatButton2.perform(click())
//
//        Intents.release()
//
//        closeSoftKeyboard()
//
//        val targetContext = InstrumentationRegistry.getTargetContext()
//        onView(withId(R.id.kinrecovery_password_edit))
//                .check(matches(hasValueEqualTo(targetContext.resources.getString(R.string.backup_and_restore_enter_password))))
//
//        onView(allOf(instanceOf(TextView::class.java),
//                withParent(withId(R.id.toolbar))))
//                .check(matches(withText(R.string.backup_and_restore_upload_qr_title)))
//
//        val stepsTextView = onView(withId(R.id.steps_text))
//        stepsTextView.check(matches(withText("")))
//                .check(matches(not(isDisplayed())))
//
//        val button = onView(
//                allOf(withId(R.id.kinrecovery_password_done_btn), isDisplayed()))
//        button.check(matches(isDisplayed()))
//                .check(matches(isEnabled()))
//    }
//
//    @Test
//    fun restoreActivity_PasswordScreenToDone_CorrectComponents() {
//        launchActivity()
//
//        Intents.init()
//        expectedIntent = intendingStubQRIntent()
//
//        val appCompatTextView = onView(
//                allOf(withId(R.id.upload_btn_text), withText(R.string.backup_and_restore_upload_qr_btn),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.fragment_frame),
//                                        0),
//                                5),
//                        isDisplayed()))
//        appCompatTextView.perform(click())
//
//        val appCompatButton2 = onView(
//                allOf(withId(android.R.id.button1), withText("OK"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.buttonPanel),
//                                        0),
//                                3)))
//        appCompatButton2.perform(click())
//
//        Intents.release()
//
//        val editText = onView(
//                allOf(childAtPosition(
//                        allOf(withId(R.id.kinrecovery_password_edit),
//                                childAtPosition(
//                                        withClassName(`is`("android.support.constraint.ConstraintLayout")),
//                                        3)),
//                        0),
//                        isDisplayed()))
//        editText.perform(typeText("qwertyU1!"), closeSoftKeyboard())
//
//        val doneButton = onView(
//                allOf(withId(R.id.kinrecovery_password_done_btn), isDisplayed()))
//        doneButton.perform(click())
//
//        onView(allOf(instanceOf(TextView::class.java),
//                withParent(withId(R.id.toolbar))))
//                .check(matches(withText(R.string.backup_and_restore_restore_completed_title)))
//
//        val stepsTextView = onView(withId(R.id.steps_text))
//        stepsTextView.check(matches(withText("")))
//                .check(matches(not(isDisplayed())))
//
//        val imageButton = onView(
//                allOf(childAtPosition(
//                        allOf(withId(R.id.toolbar),
//                                childAtPosition(
//                                        IsInstanceOf.instanceOf(android.view.ViewGroup::class.java),
//                                        0)),
//                        0)))
//        imageButton.check(matches(isTransparentDrawable(R.drawable.back)))
//
//        val button = onView(
//                allOf(withId(R.id.backup_and_restore_v_btn), isDisplayed()))
//        button.check(matches(isDisplayed()))
//                .check(matches(isEnabled()))
//        button.perform(click())
//
//        val f = Activity::class.java.getDeclaredField("mResultCode")
//        f.isAccessible = true
//        val resultCode = f.getInt(activityTestRule.activity)
//        assertEquals(resultCode, 5000) //5000 is success
//
//    }
//
//    //TODO For full coverage we still need to add method to check when pressing back from screen B then go to Screen A and everything is correct
//    //TODO Also when putting wrong password or bad QR code
//
//    private fun launchActivity() {
//        Intents.init()
//        activityTestRule.launchActivity(stubIntent)
//
//        // Check the intent is handled by the app
//        val expectedIntent = hasComponent(componentName)
//        intended(expectedIntent)
//        Intents.release()
//    }
//
//}