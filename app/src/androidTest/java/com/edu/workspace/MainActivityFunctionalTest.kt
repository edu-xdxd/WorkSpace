package com.edu.workspace

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba funcional:
 * Verifica la navegación y carga de fragmentos dentro del MainActivity
 */
@RunWith(AndroidJUnit4::class)
class MainActivityFunctionalTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun homeFragment_isDisplayedByDefault() {
        // Comprueba que el texto o elemento principal del HomeFragment aparezca
        onView(withId(R.id.tvHeaderTitle))
            .check(matches(isDisplayed()))
    }


}
