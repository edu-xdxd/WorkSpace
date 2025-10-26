package com.edu.workspace


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText

import androidx.test.espresso.intent.Intents // <-- Importante
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After // <-- Importante
import org.junit.Before // <-- Importante
import org.junit.Rule
import org.junit.Test


import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setup() {
        // Inicializa el monitoreo de Intents antes de cada prueba
        Intents.init()
    }

    @After
    fun tearDown() {
        // Libera el monitoreo de Intents después de cada prueba
        Intents.release()
    }

    @Test
    fun loginAttempt_withValidCredentials_navigatesToMainActivity() {
        onView(withId(R.id.editTextEmail))
            .perform(replaceText("ojjkd27@gmail.com"))

        onView(withId(R.id.editTextPassword))
            .perform(replaceText("eduardo1712004"))

        onView(withId(R.id.buttonLogin))
            .perform(click())

        intended(hasComponent(MainActivity::class.java.name))
    }


}