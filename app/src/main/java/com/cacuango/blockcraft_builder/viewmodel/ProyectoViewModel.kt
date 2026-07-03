package com.cacuango.blockcraft.builder.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cacuango.blockcraft.builder.data.local.database.AppDatabase
import com.cacuango.blockcraft.builder.data.local.entity.Bloque
import com.cacuango.blockcraft.builder.data.local.entity.HistorialAccion
import com.cacuango.blockcraft.builder.data.local.entity.Proyecto
import com.cacuango.blockcraft.builder.data.local.entity.TipoBloque
import com.cacuango.blockcraft.builder.data.repository.ProyectoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProyectoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProyectoRepository by lazy {
        val db = AppDatabase.getInstance(application)
        ProyectoRepository(
            proyectoDao = db.proyectoDao(),
            bloqueDao = db.bloqueDao(),
            tipoBloqueDao = db.tipoBloqueDao(),
            historialAccionDao = db.historialAccionDao()
        )
    }

    // ===== LIVEDATA =====

    // ✅ Flow reactivo — Room actualiza automáticamente sin llamadas manuales
    val proyectosLiveData: LiveData<List<Proyecto>> get() =
        repository.obtenerProyectosFlow().asLiveData()

    // LiveData para búsqueda — separado del Flow principal
    private val _proyectosBusqueda = MutableLiveData<List<Proyecto>>()
    val proyectosBusqueda: LiveData<List<Proyecto>> = _proyectosBusqueda

    private val _proyectoActual = MutableLiveData<Proyecto?>()
    val proyectoActual: LiveData<Proyecto?> = _proyectoActual

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _mensajeExito = MutableLiveData<String>()
    val mensajeExito: LiveData<String> = _mensajeExito

    private val _contadorBloques = MutableLiveData<Int>()
    val contadorBloques: LiveData<Int> = _contadorBloques

    private val _proyectoCreado = MutableLiveData<Int>()
    val proyectoCreado: LiveData<Int> = _proyectoCreado

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _tiposBloque = MutableLiveData<List<TipoBloque>>()
    val tiposBloque: LiveData<List<TipoBloque>> = _tiposBloque

    // ===== PROYECTOS =====

    fun cargarProyecto(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val proyecto = withContext(Dispatchers.IO) {
                    repository.obtenerProyectoPorId(id)
                }
                _proyectoActual.postValue(proyecto)
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al cargar proyecto", e)
                _error.postValue("No se pudo cargar el proyecto. Intenta de nuevo.")
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

                val fechaActual = System.currentTimeMillis().toString()
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

                _proyectoCreado.postValue(id.toInt())
                _mensajeExito.postValue("✅ Proyecto '$nombre' creado exitosamente")
                // ✅ Ya no necesita cargarTodosLosProyectos() — Flow lo hace solo

            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al crear proyecto", e)
                _error.postValue("No se pudo crear el proyecto. Intenta de nuevo.")
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
                        fechaModificacion = System.currentTimeMillis().toString()
                    )
                    withContext(Dispatchers.IO) {
                        repository.guardarProyecto(proyectoActualizado)
                    }
                    _mensajeExito.postValue("✅ Proyecto guardado")
                    // ✅ Flow actualiza la lista automáticamente
                } else {
                    _error.postValue("No hay proyecto para guardar")
                }
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al guardar proyecto", e)
                _error.postValue("No se pudo guardar el proyecto. Intenta de nuevo.")
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
                    // ✅ Flow actualiza la lista automáticamente
                } else {
                    _error.postValue("No se pudo eliminar el proyecto")
                }
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al eliminar proyecto", e)
                _error.postValue("No se pudo eliminar el proyecto. Intenta de nuevo.")
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
                // Búsqueda usa LiveData separado para no interferir con el Flow
                _proyectosBusqueda.postValue(proyectos)
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al buscar proyectos", e)
                _error.postValue("Error en la búsqueda. Intenta de nuevo.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filtrarProyectosPorBioma(bioma: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val proyectos = withContext(Dispatchers.IO) {
                    if (bioma == null || bioma == "Todos") {
                        repository.obtenerTodosLosProyectosSuspend() // ← cambio aquí
                    } else {
                        repository.buscarProyectosPorNombre(bioma)
                    }
                }
                _proyectosBusqueda.postValue(proyectos)
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al filtrar por bioma", e)
                _error.postValue("Error al filtrar proyectos.")
            } finally {
                _isLoading.value = false
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
                        fechaModificacion = System.currentTimeMillis().toString()
                    )
                    withContext(Dispatchers.IO) {
                        repository.guardarProyecto(actualizado)
                    }
                    _proyectoCreado.postValue(actualizado.id)
                    _mensajeExito.postValue("✅ Proyecto actualizado")
                    // ✅ Flow actualiza la lista automáticamente
                }
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al actualizar proyecto", e)
                _error.postValue("No se pudo actualizar el proyecto.")
            } finally {
                _isLoading.value = false
            }
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
                Log.e("ProyectoViewModel", "Error al agregar bloque", e)
                _error.postValue("Error al agregar bloque. Intenta de nuevo.")
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
                Log.e("ProyectoViewModel", "Error al deshacer", e)
                _error.postValue("No se pudo deshacer. Intenta de nuevo.")
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
                Log.e("ProyectoViewModel", "Error al contar bloques", e)
                _error.postValue("No se pudieron contar los bloques.")
            }
        }
    }

    // ===== TIPOS DE BLOQUE =====

    fun cargarTiposDeBloque() {
        viewModelScope.launch {
            try {
                val tipos = withContext(Dispatchers.IO) {
                    repository.obtenerTiposActivos()
                }
                _tiposBloque.postValue(tipos)
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al cargar tipos de bloque", e)
                _error.postValue("No se pudieron cargar los tipos de bloque.")
            }
        }
    }

    // ===== HISTORIAL =====

    fun registrarAccion(
        proyectoId: Int,
        tipoAccion: String,
        tipoBloque: String,
        posX: Int, posY: Int, posZ: Int,
        orden: Int
    ) {
        viewModelScope.launch {
            try {
                val accion = HistorialAccion(
                    id_proyecto = proyectoId,
                    tipo_accion = tipoAccion,
                    tipo_bloque = tipoBloque,
                    pos_x = posX,
                    pos_y = posY,
                    pos_z = posZ,
                    orden = orden,
                    fecha_hora = System.currentTimeMillis().toString()
                )
                withContext(Dispatchers.IO) {
                    repository.registrarAccion(accion)
                }
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al registrar acción", e)
            }
        }
    }

    fun deshacerAccion(proyectoId: Int) {
        viewModelScope.launch {
            try {
                val ultimaAccion = withContext(Dispatchers.IO) {
                    repository.obtenerUltimaAccion(proyectoId)
                }
                if (ultimaAccion != null && ultimaAccion.tipo_accion == "COLOCAR") {
                    withContext(Dispatchers.IO) {
                        repository.limpiarHistorial(proyectoId)
                    }
                    _mensajeExito.postValue("↩ Acción deshecha")
                    actualizarContadorBloques(proyectoId)
                } else {
                    _error.postValue("No hay acciones para deshacer")
                }
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al deshacer acción", e)
                _error.postValue("No se pudo deshacer la acción.")
            }
        }
    }

    // ===== UTILIDADES =====

    fun cambiarCategoriaBloques(categoria: String) {
        _mensajeExito.postValue("Categoría cambiada a: $categoria")
    }

    fun activarCuadricula(activa: Boolean) {
        _mensajeExito.postValue(
            if (activa) "📐 Cuadrícula activada"
            else "📐 Cuadrícula desactivada"
        )
    }

    fun compartirProyecto(proyectoId: Int) {
        viewModelScope.launch {
            try {
                val proyecto = withContext(Dispatchers.IO) {
                    repository.obtenerProyectoPorId(proyectoId)
                }
                if (proyecto != null) {
                    _mensajeExito.postValue("📤 Compartiendo: ${proyecto.nombre}")
                }
            } catch (e: Exception) {
                Log.e("ProyectoViewModel", "Error al compartir", e)
                _error.postValue("No se pudo compartir el proyecto.")
            }
        }
    }
}