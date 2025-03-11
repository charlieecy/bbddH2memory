package org.example

import org.example.models.Vehiculo
import org.example.repository.VehiculoRepositoryImpl
import java.sql.DriverManager
import java.time.LocalDate


fun main() {
    val repository = VehiculoRepositoryImpl()

    //Url para crear database h2 en memoria
    //OJO, le damos un nombre "miPrimeraBBDD" y ponemos ";DB_CLOSE_DELAY=-1" porque, como estamos trabajando con una base
    //en memoria, por defecto, se borraría toda la BBDD cada vez que cerremos la conexión. Con ello, conseguimos que la
    //BBDD se borre una vez que la JVM para, o sea, cuando finaliza nuestro programa.
    val h2databaseInMemoryURL = "jdbc:h2:mem:miPrimeraBBDD;DB_CLOSE_DELAY=-1"

    //Abrimos conexión
    val connection = DriverManager.getConnection(h2databaseInMemoryURL)
    //Comprobamos si tenemos conexión
    if (connection == null) {
        println("NO se ha podido conectar")
    } else {
        println("¡Conexión establecida!")
    }

    //Creamos la tabla para guardar vehículos, con los tipos de datos de h2 que equivalen a los del modelo en Kotlin
    val createTableStatement = """CREATE TABLE IF NOT EXISTS vehiculos (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        matricula VARCHAR not null,
        marca VARCHAR not null,
        modelo VARCHAR not null,
        fechaMatriculacion VARCHAR not null,
        permisoActivo BOOLEAN not null,
        tipo VARCHAR not null
        );
        """.trimIndent()

    //Da 0 porque al crear la tabla no se modifica ninguna fila, sólo se crea la estructura
    val resultado = connection.createStatement().executeUpdate(createTableStatement)
    println("Nº de filas modificadas por la creación de la tabla: $resultado")

    //Cerramos conexión
    connection.close()
    //Comprobamos si se ha cerrado
    if (connection.isClosed) {
        println("¡Conexión cerrada con éxito!")
    } else {
        println("La conexión sigue abierta...")
    }

    println()

    val v1 = Vehiculo(
        matricula = "4422KJM",
        marca = "Ford",
        modelo = "Mondeo",
        fechaMatriculacion = LocalDate.of(2024, 10, 25),
        permisoActivo = true,
        tipo = Vehiculo.Tipo.COMBUSTION
    )

    val v2 = Vehiculo(
        matricula = "5572JKB",
        marca = "Toyota",
        modelo = "Prius",
        fechaMatriculacion = LocalDate.of(2022, 8, 25),
        permisoActivo = true,
        tipo = Vehiculo.Tipo.ELECTRICO
    )

    val v3 = Vehiculo(
        matricula = "1234ABC",
        marca = "Ford",
        modelo = "Focus",
        fechaMatriculacion = LocalDate.of(2020, 5, 14),
        permisoActivo = true,
        tipo = Vehiculo.Tipo.HIBRIDO
    )

    //Guardamos 3 vehículos
    repository.save(v1)
    repository.save(v2)
    repository.save(v3)

    println()

    //Obtenemos todos los vehículos
    repository.getAll().forEach { println(it) }

    println()

    //Buscamos por id (existe)
    println(repository.getById(3))
    println()
    //Buscamos por id (no existe)
    println(repository.getById(4))

    println()

    //Borramos por id (existe)
    println(repository.delete(1))
    repository.getAll().forEach { println(it) }
    println()
    //Borramos por id (no existe)
    println(repository.delete(4))

    println()

    //Actualizamos vehículo
    println(repository.update(3, v3))


}