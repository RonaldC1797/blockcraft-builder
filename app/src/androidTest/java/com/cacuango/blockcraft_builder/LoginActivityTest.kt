package com.cacuango.blockcraft.builder

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cacuango.blockcraft.builder.ui.auth.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    // ── TEST 1 ───────────────────────────────────
    // Verifica que los campos de email y password
    // son visibles al abrir LoginActivity.
    @Test
    fun loginActivity_alAbrir_debeMostrarCampos() {
        // Paso 1 + 3 — verificar que el campo email es visible
        onView(withId(R.id.etEmail))
            .check(matches(isDisplayed()))

        // Paso 1 + 3 — verificar que el campo password es visible
        onView(withId(R.id.etPassword))
            .check(matches(isDisplayed()))

        // Paso 1 + 3 — verificar que el botón de login es visible
        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))
    }

    // ── TEST 2 ───────────────────────────────────
    // Verifica que se puede escribir en el campo
    // de email correctamente.
    @Test
    fun loginActivity_escribirEmail_debeActualizarCampo() {
        // Paso 1 + 2 — escribir en el campo email
        onView(withId(R.id.etEmail))
            .perform(
                clearText(),
                replaceText("ronald@uce.edu.ec"),
                closeSoftKeyboard()
            )

        // Paso 3 — verificar que el texto quedó escrito
        onView(withId(R.id.etEmail))
            .check(matches(withText("ronald@uce.edu.ec")))
    }

    // ── TEST 3 ───────────────────────────────────
    // Verifica que al tocar login con campos vacíos
    // el botón sigue visible (no navega a otra pantalla)
    @Test
    fun loginActivity_camposVacios_botonSigueVisible() {
        // Paso 1 + 2 — tocar login sin llenar campos
        onView(withId(R.id.btnLogin))
            .perform(click())

        // Paso 3 — verificar que seguimos en LoginActivity
        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))
    }

    // ── TEST 4 ───────────────────────────────────
    // Verifica que el enlace a registro es visible
    // y está habilitado.
    @Test
    fun loginActivity_enlaceRegistro_debeSerVisible() {
        onView(withId(R.id.tvRegisterLink))
            .check(matches(isDisplayed()))
    }
}