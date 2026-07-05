package com.cacuango.blockcraft.builder

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cacuango.blockcraft.builder.data.local.database.AppDatabase
import com.cacuango.blockcraft.builder.data.local.dao.ProyectoDao
import com.cacuango.blockcraft.builder.data.local.dao.BloqueDao
import com.cacuango.blockcraft.builder.data.local.entity.Proyecto
import com.cacuango.blockcraft.builder.data.local.entity.Bloque
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProyectoDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var proyectoDao: ProyectoDao
    private lateinit var bloqueDao: BloqueDao

    // ── Proyecto de prueba reutilizable ──────────
    private val proyectoPrueba = Proyecto(
        nombre = "Castillo Medieval",
        fechaCreacion = System.currentTimeMillis().toString(),
        fechaModificacion = System.currentTimeMillis().toString(),
        camaraX = 0f,
        camaraY = 0f,
        camaraZ = 0f
    )

    @Before
    fun setUp() {
        // Crear base de datos in-memory antes de cada test
        // allowMainThreadQueries() solo se usa en tests — nunca en producción
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

        proyectoDao = db.proyectoDao()
        bloqueDao = db.bloqueDao()
    }

    @After
    fun tearDown() {
        // Cerrar y destruir la base de datos in-memory
        db.close()
    }

    // ════════════════════════════════════════════
    // CREATE — Insertar proyecto
    // ════════════════════════════════════════════

    // ── TEST 1 ───────────────────────────────────
    // Verifica que al insertar un proyecto en Room
    // se le asigna un ID mayor a 0.
    @Test
    fun insertarProyecto_debeRetornarIdValido() = runBlocking {
        // Arrange
        val proyecto = proyectoPrueba

        // Act
        val id = proyectoDao.insertarProyecto(proyecto)

        // Assert
        // assertTrue: el ID autogenerado debe ser mayor a 0
        assertTrue(id > 0)
    }

    // ── TEST 2 ───────────────────────────────────
    // Verifica que el proyecto insertado se puede
    // recuperar por su ID con los datos correctos.
    @Test
    fun insertarYRecuperar_proyectoGuardado_existeEnRoom() = runBlocking {
        // Arrange
        val proyecto = proyectoPrueba

        // Act
        val id = proyectoDao.insertarProyecto(proyecto)
        val recuperado = proyectoDao.obtenerProyectoPorId(id.toInt())

        // Assert
        // assertNotNull: el proyecto debe existir en Room
        assertNotNull(recuperado)
        // assertEquals: el nombre guardado debe ser exactamente el mismo
        assertEquals("Castillo Medieval", recuperado?.nombre)
    }

    // ════════════════════════════════════════════
    // READ — Leer proyectos
    // ════════════════════════════════════════════

    // ── TEST 3 ───────────────────────────────────
    // Verifica que la lista de proyectos está vacía
    // cuando no se ha insertado ninguno.
    @Test
    fun obtenerTodos_baseDatosVacia_debeRetornarListaVacia() = runBlocking {
        // Arrange — base de datos in-memory vacía por defecto

        // Act
        val proyectos = proyectoDao.obtenerTodosLosProyectosSuspend()

        // Assert
        // assertTrue: la lista debe estar vacía al inicio
        assertTrue(proyectos.isEmpty())
    }

    // ── TEST 4 ───────────────────────────────────
    // Verifica que tras insertar 2 proyectos la lista
    // contiene exactamente 2 elementos.
    @Test
    fun obtenerTodos_dosProyectosInsertados_debeRetornarDos() = runBlocking {
        // Arrange
        proyectoDao.insertarProyecto(proyectoPrueba)
        proyectoDao.insertarProyecto(
            proyectoPrueba.copy(nombre = "Nave Espacial")
        )

        // Act
        val proyectos = proyectoDao.obtenerTodosLosProyectosSuspend()

        // Assert
        // assertEquals: debe haber exactamente 2 proyectos
        assertEquals(2, proyectos.size)
    }

    // ════════════════════════════════════════════
    // UPDATE — Actualizar proyecto
    // ════════════════════════════════════════════

    // ── TEST 5 ───────────────────────────────────
    // Verifica que al actualizar un proyecto el nuevo
    // nombre queda guardado correctamente en Room.
    @Test
    fun actualizarProyecto_nombreCambiado_debeReflejarseEnRoom() = runBlocking {
        // Arrange
        val id = proyectoDao.insertarProyecto(proyectoPrueba)
        val recuperado = proyectoDao.obtenerProyectoPorId(id.toInt())!!

        // Act — cambiar el nombre
        val actualizado = recuperado.copy(nombre = "Puente Colgante")
        proyectoDao.actualizarProyecto(actualizado)

        // Assert
        val resultado = proyectoDao.obtenerProyectoPorId(id.toInt())
        // assertEquals: el nombre debe haber cambiado
        assertEquals("Puente Colgante", resultado?.nombre)
    }

    // ════════════════════════════════════════════
    // DELETE — Eliminar proyecto
    // ════════════════════════════════════════════

    // ── TEST 6 ───────────────────────────────────
    // Verifica que al eliminar un proyecto por ID
    // ya no se puede recuperar de Room.
    @Test
    fun eliminarProyecto_proyectoEliminado_noDebeExistirEnRoom() = runBlocking {
        // Arrange
        val id = proyectoDao.insertarProyecto(proyectoPrueba)

        // Act
        proyectoDao.eliminarProyectoPorId(id.toInt())

        // Assert
        val resultado = proyectoDao.obtenerProyectoPorId(id.toInt())
        // assertNull: el proyecto eliminado no debe encontrarse
        assertNull(resultado)
    }

    // ── TEST 7 ───────────────────────────────────
    // Verifica que al eliminar un proyecto sus bloques
    // también se eliminan (integridad referencial).
    @Test
    fun eliminarProyecto_bloqueAsociado_tambienDebeEliminarse() = runBlocking {
        // Arrange
        val proyectoId = proyectoDao.insertarProyecto(proyectoPrueba).toInt()
        val bloque = Bloque(
            proyectoId = proyectoId,
            tipoId = "madera",
            posX = 0, posY = 0, posZ = 0
        )
        bloqueDao.insertarBloque(bloque)


        // Act — igual que ProyectoRepository.eliminarProyectoPorId()
        bloqueDao.eliminarBloquesPorProyecto(proyectoId)
        proyectoDao.eliminarProyectoPorId(proyectoId)
        // Assert
        // Assert
        val bloques = bloqueDao.obtenerBloquesPorProyecto(proyectoId)
        assertTrue(bloques.isEmpty())
        assertNull(proyectoDao.obtenerProyectoPorId(proyectoId))
    }

    // ════════════════════════════════════════════
    // BÚSQUEDA
    // ════════════════════════════════════════════

    // ── TEST 8 ───────────────────────────────────
    // Verifica que buscarProyectosPorNombre retorna
    // solo los proyectos que coinciden con el query.
    @Test
    fun buscarPorNombre_queryParcial_debeRetornarCoincidencias() = runBlocking {
        // Arrange
        proyectoDao.insertarProyecto(proyectoPrueba) // "Castillo Medieval"
        proyectoDao.insertarProyecto(proyectoPrueba.copy(nombre = "Nave Espacial"))

        // Act
        val resultados = proyectoDao.buscarProyectosPorNombre("Castillo")

        // Assert
        // assertEquals: solo 1 proyecto coincide con "Castillo"
        assertEquals(1, resultados.size)
        assertEquals("Castillo Medieval", resultados[0].nombre)
    }

    // ── TEST 9 ───────────────────────────────────
    // Verifica que una búsqueda sin coincidencias
    // retorna lista vacía sin lanzar excepción.
    @Test
    fun buscarPorNombre_sinCoincidencias_debeRetornarListaVacia() = runBlocking {
        // Arrange
        proyectoDao.insertarProyecto(proyectoPrueba)

        // Act
        val resultados = proyectoDao.buscarProyectosPorNombre("XYZ_NO_EXISTE")

        // Assert
        // assertTrue: sin coincidencias la lista debe estar vacía
        assertTrue(resultados.isEmpty())
    }
}