package com.cacuango.blockcraft.builder

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        Log.d("MI_APP", "🔵 RegisterActivity onCreate")

        auth = FirebaseAuth.getInstance()
        Log.d("MI_APP", "✅ FirebaseAuth inicializado")

        val tilName            = findViewById<TextInputLayout>(R.id.tilName)
        val tilEmail           = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword        = findViewById<TextInputLayout>(R.id.tilPassword)
        val tilConfirmPassword = findViewById<TextInputLayout>(R.id.tilConfirmPassword)
        val btnRegister        = findViewById<MaterialButton>(R.id.btnRegister)
        val tvLoginLink        = findViewById<TextView>(R.id.tvLoginLink)

        // =========================================
        // LIMPIAR ERRORES MIENTRAS EL USUARIO ESCRIBE
        // =========================================
        tilName.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (tilName.error != null) tilName.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        tilEmail.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (tilEmail.error != null) tilEmail.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        tilPassword.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (tilPassword.error != null) tilPassword.error = null
                if (tilConfirmPassword.error != null &&
                    tilConfirmPassword.editText?.text?.isNotEmpty() == true) {
                    tilConfirmPassword.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        tilConfirmPassword.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (tilConfirmPassword.error != null) tilConfirmPassword.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // =========================================
        // BOTÓN REGISTRO
        // =========================================
        btnRegister.setOnClickListener {
            val name    = tilName.editText?.text.toString().trim()
            val email   = tilEmail.editText?.text.toString().trim()
            val password        = tilPassword.editText?.text.toString()
            val confirmPassword = tilConfirmPassword.editText?.text.toString()

            Log.d("MI_APP", "🔵 Botón Registro presionado")
            Log.d("MI_APP", "📛 Nombre ingresado: '$name'")
            Log.d("MI_APP", "📧 Email ingresado: '$email'")

            if (name.isEmpty()) {
                tilName.error = "El nombre es requerido"
                return@setOnClickListener
            }
            if (name.length < 3) {
                tilName.error = "El nombre es muy corto (mínimo 3 caracteres)"
                return@setOnClickListener
            }
            if (!name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))) {
                tilName.error = "Solo letras y espacios"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                tilEmail.error = "El email es requerido"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Email inválido (ej: usuario@dominio.com)"
                return@setOnClickListener
            }
            if (email.contains(" ")) {
                tilEmail.error = "El email no puede tener espacios"
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
            if (password.length > 30) {
                tilPassword.error = "Máximo 30 caracteres"
                return@setOnClickListener
            }
            if (!password.any { it.isDigit() }) {
                tilPassword.error = "Debe contener al menos un número"
                return@setOnClickListener
            }
            if (!password.any { it.isUpperCase() }) {
                tilPassword.error = "Debe contener al menos una mayúscula"
                return@setOnClickListener
            }
            if (!password.any { it.isLowerCase() }) {
                tilPassword.error = "Debe contener al menos una minúscula"
                return@setOnClickListener
            }
            if (confirmPassword.isEmpty()) {
                tilConfirmPassword.error = "Confirma tu contraseña"
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                tilConfirmPassword.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }

            tilName.error            = null
            tilEmail.error           = null
            tilPassword.error        = null
            tilConfirmPassword.error = null

            Log.d("MI_APP", "✅ Validaciones pasadas, registrando usuario...")
            registrarUsuario(email, password, name)
        }

        tvLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun registrarUsuario(email: String, password: String, name: String) {
        Log.d("MI_APP", "🔵 Registrar usuario: $email con nombre: $name")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MI_APP", "✅ Usuario creado exitosamente en Firebase")

                    val user = auth.currentUser
                    Log.d("MI_APP", "👤 Usuario: ${user?.email}")

                    // 🔥 ACTUALIZAR PERFIL CON EL NOMBRE
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    Log.d("MI_APP", "📝 Actualizando perfil con nombre: '$name'")

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d("MI_APP", "✅ ✅ ✅ NOMBRE GUARDADO EXITOSAMENTE: '$name'")
                                Toast.makeText(this,
                                    "¡Registro exitoso! Bienvenido $name",
                                    Toast.LENGTH_LONG).show()
                            } else {
                                Log.e("MI_APP", "❌ Error al guardar nombre: ${updateTask.exception?.message}")
                                Toast.makeText(this,
                                    "Registro exitoso, pero no se pudo guardar el nombre",
                                    Toast.LENGTH_LONG).show()
                            }

                            // 🔥 VERIFICAR QUE EL NOMBRE SE GUARDÓ
                            val userAfterUpdate = auth.currentUser
                            Log.d("MI_APP", "🔍 Nombre en Firebase después de update: ${userAfterUpdate?.displayName}")

                            // Navegar a MainActivity
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                } else {
                    Log.e("MI_APP", "❌ Error al crear usuario: ${task.exception?.message}")
                    manejarErrorRegistro(task.exception)
                }
            }
    }

    private fun manejarErrorRegistro(exception: Exception?) {
        val tilEmail    = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)

        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                tilPassword.error = "La contraseña es muy débil (mínimo 6 caracteres)"
            }
            is FirebaseAuthUserCollisionException -> {
                tilEmail.error = "Este email ya está registrado"
            }
            else -> {
                Toast.makeText(this,
                    "Error: ${exception?.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}