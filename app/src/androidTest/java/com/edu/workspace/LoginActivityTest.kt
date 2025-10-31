package com.edu.workspace

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import android.view.View
import org.hamcrest.Matcher
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    /**
     * Espera personalizada para dar tiempo a que las vistas carguen
     */
    private fun waitForView(timeout: Long = 30000): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isRoot()
            override fun getDescription() = "Esperar $timeout ms para que la vista cargue"
            override fun perform(uiController: UiController?, view: View?) {
                uiController?.loopMainThreadForAtLeast(timeout)
            }
        }
    }

    /**
     * Verifica que al presionar el texto "SignUp" se abra la pantalla de registro
     */
    @Test
    fun loginAttempt_withValidCredentials_triggersIntentToRegistroActivity() {
        Thread.sleep(30000)

        // Espera a que la pantalla cargue
        onView(isRoot()).perform(waitForView())

        // Simula clic en el texto "¿No tienes cuenta? Regístrate"
        onView(withId(R.id.textViewSignUp))
            .perform(click())

        // Verifica que se haya lanzado el intent hacia RegistroActivity
        intended(hasComponent(RegistroActivity::class.java.name))
    }

}
