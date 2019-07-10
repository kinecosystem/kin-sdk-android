package kin.backup.and.restore.ui.tests

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import kin.backup.and.restore.ui.tests.RestoreActivityFullHappyPathTest.Constants.PUBLIC_ADDRESS
import kin.backup.and.restore.ui.tests.UiTestUtils.childAtPosition
import kin.backup.and.restore.ui.tests.UiTestUtils.intendingStubQRIntent
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@LargeTest
@RunWith(AndroidJUnit4::class)
class RestoreActivityFullHappyPathTest {


    @get:Rule
    var activityTestRule = IntentsTestRule(BackupAndRestoreDummyActivityForUITest::class.java)

    @Before
    @Throws(IOException::class)
    fun setup() {
        intendingStubQRIntent()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun restoreActivityFullHappyPathTest() {
        val appCompatButton = onView(
                allOf(withId(R.id.restore_account), withText("Restore Account"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                7),
                        isDisplayed()))
        appCompatButton.perform(click())

        val appCompatTextView = onView(
                allOf(withId(R.id.upload_btn_text), withText(R.string.backup_and_restore_upload_qr_btn),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_frame),
                                        0),
                                5),
                        isDisplayed()))
        appCompatTextView.perform(click())

        val appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)))
        appCompatButton2.perform(scrollTo(), click())

        val editText = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.kinrecovery_password_edit),
                                childAtPosition(
                                        withClassName(`is`("android.support.constraint.ConstraintLayout")),
                                        3)),
                        0),
                        isDisplayed()))
        editText.perform(typeText("qwertyU1!"), closeSoftKeyboard())

        val appCompatButton3 = onView(
                allOf(withId(R.id.kinrecovery_password_done_btn), withText(R.string.backup_and_restore_done),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_frame),
                                        0),
                                4),
                        isDisplayed()))
        appCompatButton3.perform(click())

        val appCompatImageView = onView(
                allOf(withId(R.id.backup_and_restore_v_btn),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_frame),
                                        0),
                                3),
                        isDisplayed()))
        appCompatImageView.perform(click())

        val textView = onView(
                allOf(withId(R.id.public_address_value), withText(PUBLIC_ADDRESS),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()))
        textView.check(matches(withText(PUBLIC_ADDRESS)))
    }

    object Constants {
        const val PUBLIC_ADDRESS = "GDQG4RKOSETYI6JVL3ERNE3WSGFA56C5JLLR2MMNQZL7FD7BEJE2YRCT"
    }

}
