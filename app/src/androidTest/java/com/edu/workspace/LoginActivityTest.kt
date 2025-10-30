package com.edu.workspace

import android.app.Instrumentation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setup() {
        // Inicializa el sistema de Intents para monitorear y controlar los Intents salientes
        Intents.init()

        // Simula (intercepta) el Intent hacia MainActivity sin lanzar realmente esa Activity
       /* intending(hasComponent(MainActivity::class.java.name)).respondWith(
            Instrumentation.ActivityResult(0, null)
        )*/
    }

    @After
    fun tearDown() {
        // Libera el sistema de Intents después de cada prueba
        Intents.release()
    }

    @Test
    fun loginAttempt_withValidCredentials_triggersIntentToMainActivity() {
        // Simula ingreso de credenciales válidas
        onView(withId(R.id.editTextEmail))
            .perform(replaceText("ojjkd27@gmail.com"))

        onView(withId(R.id.editTextPassword))
            .perform(replaceText("eduardo1712004"))

        // Simula clic en botón de login
        onView(withId(R.id.buttonLogin))
            .perform(click())

        // Verifica que se haya lanzado un Intent hacia MainActivity
        intended(hasComponent(MainActivity::class.java.name))
    }
}
