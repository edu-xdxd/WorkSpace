package com.edu.workspace

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import android.view.View
import org.hamcrest.Matcher
import org.junit.*
import org.junit.runner.RunWith

/**
 * Prueba funcional:
 * Verifica la navegación y carga de fragmentos dentro del MainActivity
 */
@RunWith(AndroidJUnit4::class)
class MainActivityFunctionalTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private fun waitForView(timeout: Long = 3000): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isRoot()
            override fun getDescription() = "Esperar $timeout ms para que la vista cargue"
            override fun perform(uiController: UiController?, view: View?) {
                uiController?.loopMainThreadForAtLeast(timeout)
            }
        }
    }

    /**
     * Verifica que el HomeFragment se muestre por defecto
     */
    @Test
    fun homeFragment_isDisplayedByDefault() {
        onView(isRoot()).perform(waitForView())
        onView(withId(R.id.tvHeaderTitle))
            .check(matches(isDisplayed()))
    }

}
