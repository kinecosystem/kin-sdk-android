package kin.backup.and.restore.ui.tests


import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.UiDevice
import kin.backup.and.restore.ui.tests.UiTestUtils.childAtPosition
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class BackupActivityFullHappyPathTest {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(BackupAndRestoreDummyActivityForUITest::class.java)

    @After
    fun tearDown() {
    }

    @Test
    fun backupActivityFullHappyPathTest() {
        val appCompatButton = onView(
                allOf(withId(R.id.create_new_account), withText("Create New Account"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()))
        appCompatButton.perform(click())

        val appCompatButton2 = onView(
                allOf(withId(R.id.backup_current_account), withText("Backup Current Account"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()))
        appCompatButton2.perform(click())

        val appCompatButton3 = onView(
                allOf(withId(R.id.lets_go_button), withText("Let's Go"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_frame),
                                        0),
                                3),
                        isDisplayed()))
        appCompatButton3.perform(click())

        pressBack()

        val editText = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.enter_pass_edittext),
                                childAtPosition(
                                        withClassName(`is`("android.support.constraint.ConstraintLayout")),
                                        2)),
                        0),
                        isDisplayed()))
        editText.perform(click())

        val editText2 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.enter_pass_edittext),
                                childAtPosition(
                                        withClassName(`is`("android.support.constraint.ConstraintLayout")),
                                        2)),
                        0),
                        isDisplayed()))
        editText2.perform(replaceText("q"), closeSoftKeyboard())

        val editText3 = onView(
                allOf(withText("q"),
                        isDisplayed()))
        editText3.perform(replaceText("qw"))

        val editText4 = onView(
                allOf(withText("qw"),
                        isDisplayed()))
        editText4.perform(replaceText("qwe"))

        val editText7 = onView(
                allOf(withText("qwe"),
                        isDisplayed()))
        editText7.perform(replaceText("qwer"))

        val editText9 = onView(
                allOf(withText("qwer"),
                        isDisplayed()))
        editText9.perform(replaceText("qwert"))

        val editText10 = onView(
                allOf(withText("qwert"),
                        isDisplayed()))
        editText10.perform(closeSoftKeyboard())

        val editText11 = onView(
                allOf(withText("qwert"),
                        isDisplayed()))
        editText11.perform(replaceText("qwerty"))

        val editText12 = onView(
                allOf(withText("qwerty"),
                        isDisplayed()))
        editText12.perform(closeSoftKeyboard())
        editText12.perform(replaceText("qwertyU"))

        val editText14 = onView(
                allOf(withText("qwertyU"),
                        isDisplayed()))
        editText14.perform(closeSoftKeyboard())
        editText14.perform(replaceText("qwertyU1"))

        val editText16 = onView(
                allOf(withText("qwertyU1"),
                        isDisplayed()))
        editText16.perform(closeSoftKeyboard())
        editText16.perform(replaceText("qwertyU1!"))

        val editText18 = onView(
                allOf(withText("qwertyU1!"),
                        isDisplayed()))
        editText18.perform(closeSoftKeyboard())

        val editText19 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.confirm_pass_edittext),
                                childAtPosition(
                                        withClassName(`is`("android.support.constraint.ConstraintLayout")),
                                        3)),
                        0),
                        isDisplayed()))
        editText19.perform(replaceText("q"), closeSoftKeyboard())

        val editText20 = onView(
                allOf(withText("q"),
                        isDisplayed()))
        editText20.perform(replaceText("qw"))

        val editText21 = onView(
                allOf(withText("qw"),
                        isDisplayed()))
        editText21.perform(closeSoftKeyboard())
        editText21.perform(replaceText("qwe"))

        val editText23 = onView(
                allOf(withText("qwe"),
                        isDisplayed()))
        editText23.perform(closeSoftKeyboard())
        editText23.perform(replaceText("qwer"))

        val editText25 = onView(
                allOf(withText("qwer"),
                        isDisplayed()))
        editText25.perform(closeSoftKeyboard())
        editText25.perform(replaceText("qwert"))

        val editText27 = onView(
                allOf(withText("qwert"),
                        isDisplayed()))
        editText27.perform(closeSoftKeyboard())
        editText27.perform(replaceText("qwerty"))

        val editText29 = onView(
                allOf(withText("qwerty"),
                        isDisplayed()))
        editText29.perform(closeSoftKeyboard())
        editText29.perform(replaceText("qwertyU"))

        val editText31 = onView(
                allOf(withText("qwertyU"),
                        isDisplayed()))
        editText31.perform(closeSoftKeyboard())
        editText31.perform(replaceText("qwertyU1"))

        val editText33 = onView(
                allOf(withText("qwertyU1"),
                        isDisplayed()))
        editText33.perform(replaceText("qwertyU1!"))

        onView(allOf(withText("qwertyU1!"),
                isDisplayed()))

        val appCompatCheckBox = onView(
                allOf(withId(R.id.understand_checkbox),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_frame),
                                        0),
                                4),
                        isDisplayed()))
        appCompatCheckBox.perform(click())

        val appCompatButton4 = onView(
                allOf(withId(R.id.next_button), withText("Next"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_frame),
                                        0),
                                6),
                        isDisplayed()))
        appCompatButton4.perform(click())

        val appCompatButton5 = onView(
                allOf(withId(R.id.send_email_button), withText("Email QR code"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_frame),
                                        0),
                                5),
                        isDisplayed()))
        appCompatButton5.perform(click())

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressBack()

        val appCompatCheckBox2 = onView(
                allOf(withId(R.id.i_saved_my_qr_checkbox),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_frame),
                                        0),
                                3),
                        isDisplayed()))

        appCompatCheckBox2.perform(click())

        val appCompatImageButton = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.toolbar),
                                childAtPosition(
                                        withClassName(`is`("android.support.constraint.ConstraintLayout")),
                                        0)),
                        0),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        val textView = onView(
                allOf(withId(R.id.backup_status_value), withText("SUCCEEDED"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()))
        textView.check(matches(withText("SUCCEEDED")))
    }

}
