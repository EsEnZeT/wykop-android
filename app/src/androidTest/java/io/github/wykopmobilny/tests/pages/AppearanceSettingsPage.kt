package io.github.wykopmobilny.tests.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.github.wykopmobilny.utils.waitVisible

object AppearanceSettingsPage {

    fun assertVisible() {
        onView(withText("Ustawienia")).waitVisible().check(matches(isDisplayed()))
    }
}
