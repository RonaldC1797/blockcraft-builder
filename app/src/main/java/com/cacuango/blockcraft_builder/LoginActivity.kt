package com.cacuango.blockcraft.builder

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val tilEmail    = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val btnLogin    = findViewById<MaterialButton>(R.id.btnLogin)
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)

        tilEmail.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (tilEmail.error != null)    tilEmail.error    = null
                if (tilPassword.error != null) tilPassword.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        tilPassword.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (tilPassword.error != null) tilPassword.error = null
                if (tilEmail.error != null)    tilEmail.error    = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnLogin.setOnClickListener {
            val email = tilEmail.editText?.text.toString().trim()

            // ✅ CORRECCIÓN 3: sin .trim() en la contraseña
            // La contraseña debe tomarse exactamente como el usuario la escribió
            val password = tilPassword.editText?.text.toString()

            if (email.isEmpty()) {
                tilEmail.error = "El email es requerido"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Email inválido"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                tilPassword.error = "La contraseña es requerida"
                return@setOnClickListener
            }
            if (password.length < 6) {
                tilPassword.error = "Mínimo 6 caracteres"
                return@setOnClickListener
            }

            tilEmail.error    = null
            tilPassword.error = null

            iniciarSesion(email, password)
        }

        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun iniciarSesion(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user   = auth.currentUser
                    val nombre = user?.displayName ?: "Usuario"
                    Toast.makeText(this, "¡Bienvenido $nombre!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this,
                        "El correo o contraseña son incorrectos.",
                        Toast.LENGTH_LONG).show()
                    manejarErrorLogin(task.exception)
                }
            }
    }

    private fun manejarErrorLogin(exception: Exception?) {
        val tilEmail    = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)

        tilEmail.error    = null
        tilPassword.error = null

        when (exception) {
            is FirebaseAuthInvalidUserException -> {
                tilEmail.error = "❌ Este correo no está registrado"
                tilPassword.editText?.text?.clear()
                tilEmail.requestFocus()
            }
            is FirebaseAuthInvalidCredentialsException -> {
                tilPassword.error = "❌ Contraseña incorrecta"
                tilPassword.editText?.text?.clear()
                tilPassword.requestFocus()
            }
            else -> {
                val mensaje = when {
                    exception?.message?.contains("too many requests") == true ->
                        "Demasiados intentos. Espera un momento."
                    exception?.message?.contains("network") == true ->
                        "Error de conexión. Verifica tu internet."
                    else -> "Error: ${exception?.message}"
                }
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}