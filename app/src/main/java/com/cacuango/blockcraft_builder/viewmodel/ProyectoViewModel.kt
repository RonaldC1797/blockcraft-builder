// viewmodel/ProyectoViewModel.kt
package com.cacuango.blockcraft.builder.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cacuango.blockcraft.builder.data.local.database.AppDatabase
import com.cacuango.blockcraft.builder.data.local.entity.Bloque
import com.cacuango.blockcraft.builder.data.local.entity.Proyecto
import com.cacuango.blockcraft.builder.data.repository.ProyectoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProyectoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProyectoRepository by lazy {
        val db = AppDatabase.getInstance(application)
        ProyectoRepository(
            proyectoDao = db.proyectoDao(),
            bloqueDao = db.bloqueDao()
        )
    }

    // ===== LIVEDATA =====
    private val _proyectosLiveData = MutableLiveData<List<Proyecto>>()
    val proyectosLiveData: LiveData<List<Proyecto>> = _proyectosLiveData

    private val _proyectoActual = MutableLiveData<Proyecto?>()
    val proyectoActual: LiveData<Proyecto?> = _proyectoActual

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _mensajeExito = MutableLiveData<String>()
    val mensajeExito: LiveData<String> = _mensajeExito

    private val _contadorBloques = MutableLiveData<Int>()
    val contadorBloques: LiveData<Int> = _contadorBloques

    // ✅ NUEVOS LIVEDATA
    private val _proyectoCreado = MutableLiveData<Int>()
    val proyectoCreado: LiveData<Int> = _proyectoCreado

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        cargarTodosLosProyectos()
    }

    // ===== PROYECTOS =====

    fun cargarTodosLosProyectos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val proyectos = withContext(Dispatchers.IO) {
                    repository.obtenerTodosLosProyectos()
                }
                _proyectosLiveData.postValue(proyectos)
            } catch (e: Exception) {
                _error.postValue("Error al cargar proyectos: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarProyecto(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val proyecto = withContext(Dispatchers.IO) {
                    repository.obtenerProyectoPorId(id)
                }
                _proyectoActual.postValue(proyecto)
            } catch (e: Exception) {
                _error.postValue("Error al cargar proyecto: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun crearProyecto(nombre: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (nombre.isEmpty()) {
                    _error.postValue("El nombre no puede estar vacío")
                    _isLoading.value = false
                    return@launch
                }

                if (nombre.length < 3) {
                    _error.postValue("El nombre debe tener al menos 3 caracteres")
                    _isLoading.value = false
                    return@launch
                }

                val fechaActual = java.util.Date().toString()
                val proyecto = Proyecto(
                    nombre = nombre,
                    fechaCreacion = fechaActual,
                    fechaModificacion = fechaActual,
                    camaraX = 0f,
                    camaraY = 0f,
                    camaraZ = 0f
                )

                val id = withContext(Dispatchers.IO) {
                    repository.guardarProyecto(proyecto)
                }

                // ✅ POSTEAR EL ID CREADO
                _proyectoCreado.postValue(id.toInt())
                _mensajeExito.postValue("✅ Proyecto '$nombre' creado exitosamente")
                cargarTodosLosProyectos()

            } catch (e: Exception) {
                _error.postValue("❌ Error al crear proyecto: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarProyecto(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val proyectoActual = _proyectoActual.value
                if (proyectoActual != null) {
                    val proyectoActualizado = proyectoActual.copy(
                        fechaModificacion = java.util.Date().toString()
                    )
                    withContext(Dispatchers.IO) {
                        repository.guardarProyecto(proyectoActualizado)
                    }
                    _mensajeExito.postValue("✅ Proyecto guardado")
                } else {
                    _error.postValue("No hay proyecto para guardar")
                }
            } catch (e: Exception) {
                _error.postValue("❌ Error al guardar proyecto: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarProyecto(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val exito = withContext(Dispatchers.IO) {
                    repository.eliminarProyectoPorId(id)
                }
                if (exito) {
                    _mensajeExito.postValue("✅ Proyecto eliminado")
                    cargarTodosLosProyectos()
                } else {
                    _error.postValue("No se pudo eliminar el proyecto")
                }
            } catch (e: Exception) {
                _error.postValue("❌ Error al eliminar proyecto: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun buscarProyectos(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val proyectos = withContext(Dispatchers.IO) {
                    repository.buscarProyectosPorNombre(query)
                }
                _proyectosLiveData.postValue(proyectos)
            } catch (e: Exception) {
                _error.postValue("Error al buscar proyectos: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filtrarProyectosPorBioma(bioma: String?) {
        cargarTodosLosProyectos()
        if (bioma != null) {
            _mensajeExito.postValue("Filtrando por: $bioma")
        }
    }

    // ===== BLOQUES =====

    fun agregarBloque(proyectoId: Int, tipoId: String, posX: Int, posY: Int, posZ: Int) {
        viewModelScope.launch {
            try {
                val bloque = Bloque(
                    proyectoId = proyectoId,
                    tipoId = tipoId,
                    posX = posX,
                    posY = posY,
                    posZ = posZ
                )
                withContext(Dispatchers.IO) {
                    repository.agregarBloque(bloque)
                }
                actualizarContadorBloques(proyectoId)
                _mensajeExito.postValue("✅ Bloque agregado")
            } catch (e: Exception) {
                _error.postValue("❌ Error al agregar bloque: ${e.message}")
            }
        }
    }

    fun deshacerUltimaAccion(proyectoId: Int) {
        viewModelScope.launch {
            try {
                val ultimoBloque = withContext(Dispatchers.IO) {
                    repository.obtenerUltimoBloque(proyectoId)
                }
                if (ultimoBloque != null) {
                    withContext(Dispatchers.IO) {
                        repository.eliminarBloquePorId(ultimoBloque.id)
                    }
                    actualizarContadorBloques(proyectoId)
                    _mensajeExito.postValue("↩ Último bloque eliminado")
                } else {
                    _error.postValue("No hay bloques para deshacer")
                }
            } catch (e: Exception) {
                _error.postValue("❌ Error al deshacer: ${e.message}")
            }
        }
    }

    fun obtenerContadorBloques(proyectoId: Int): LiveData<Int> {
        actualizarContadorBloques(proyectoId)
        return contadorBloques
    }

    private fun actualizarContadorBloques(proyectoId: Int) {
        viewModelScope.launch {
            try {
                val contador = withContext(Dispatchers.IO) {
                    repository.contarBloquesPorProyecto(proyectoId)
                }
                _contadorBloques.postValue(contador)
            } catch (e: Exception) {
                _error.postValue("Error al contar bloques: ${e.message}")
            }
        }
    }

    // ===== UTILIDADES =====

    fun cambiarCategoriaBloques(categoria: String) {
        _mensajeExito.postValue("Categoría cambiada a: $categoria")
    }

    fun activarCuadricula(activa: Boolean) {
        _mensajeExito.postValue(if (activa) "📐 Cuadrícula activada" else "📐 Cuadrícula desactivada")
    }

    fun compartirProyecto(proyectoId: Int, context: Context) {
        viewModelScope.launch {
            try {
                val proyecto = withContext(Dispatchers.IO) {
                    repository.obtenerProyectoPorId(proyectoId)
                }
                if (proyecto != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "📤 Compartiendo: ${proyecto.nombre}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                _error.postValue("Error al compartir: ${e.message}")
            }
        }
    }

    fun actualizarNombreProyecto(id: Int, nuevoNombre: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val proyecto = withContext(Dispatchers.IO) {
                    repository.obtenerProyectoPorId(id)
                }
                if (proyecto != null) {
                    val actualizado = proyecto.copy(
                        nombre = nuevoNombre,
                        fechaModificacion = java.util.Date().toString()
                    )
                    withContext(Dispatchers.IO) {
                        repository.guardarProyecto(actualizado)
                    }
                    _proyectoCreado.postValue(actualizado.id)
                    _mensajeExito.postValue("✅ Proyecto actualizado")
                    cargarTodosLosProyectos()
                }
            } catch (e: Exception) {
                _error.postValue("❌ Error al actualizar: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }


}