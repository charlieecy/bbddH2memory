package org.example.repository

import org.example.models.Vehiculo
import org.lighthousegames.logging.logging
import java.sql.DriverManager
import java.sql.Statement
import java.time.LocalDate
import java.time.LocalDateTime


class VehiculoRepositoryImpl: VehiculoRepository {
    //Url para crear database h2 en memoria
    //OJO, le damos un nombre "miPrimeraBBDD" y ponemos ";DB_CLOSE_DELAY=-1" porque, como estamos trabajando con una base
    //en memoria, por defecto, se borraría toda la BBDD cada vez que cerremos la conexión. Con ello, conseguimos que la
    //BBDD se borre una vez que la JVM para, o sea, cuando finaliza nuestro programa.
    private val h2databaseInMemoryURL = "jdbc:h2:mem:miPrimeraBBDD;DB_CLOSE_DELAY=-1"
    private val logger = logging()


    override fun save(entity: Vehiculo): Vehiculo {
        logger.debug { "Guardando vehículo en la base de datos" }

        //Creamos el vehiculo que vamos a devolver
        var vehiculo: Vehiculo? = null

        //Abrimos conexión
        val connection = DriverManager.getConnection(h2databaseInMemoryURL)
        //Comprobamos si tenemos conexión
        if (connection == null) {
            println("NO se ha podido conectar")
        } else {
            println("¡Conexión establecida!")
        }

        //Creamos la inserción en SQL
        val sql = """INSERT INTO vehiculos (matricula, marca, modelo, fechaMatriculacion, permisoActivo, tipo)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimMargin()


        //Preparamos el statement (le pasamos los parámetros ? ? ? que necesita)
        val statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, entity.matricula)
        statement.setString(2, entity.marca)
        statement.setString(3, entity.modelo)
        statement.setString(4, entity.fechaMatriculacion.toString())
        statement.setBoolean(5, entity.permisoActivo)
        statement.setString(6, entity.tipo.toString())

        //Ejecutamos el statement con los parámetros ya pasados, es decir, lo GUARDAMOS EN LA BASE DE DATOS
        val result = statement.executeUpdate()

        if(result == 1){
            println("¡Vehículo guardado con éxito!")
        }


        val generatedKeys = statement.generatedKeys

        //Como el id es autonumérico y generado por la BBDD, necesitamos obtener (getLong()) su valor para pasarlo
        //al vehículo de nuestro modelo. En este caso el columnIndex es el 1 porque el id es la primera columa de la
        //tabla vehículos. Si quisiéramos obtener, por ejemplo, la marca que se ha insertado en la tabla, tendríamos que
        //poner index 2.

        //Al igual que el index es la columna, necesitamos ejecutar .next() una vez para que el cursor pase a la primera
        //fila de la tabla, ya que, por defecto, está posicionado antes de la primera fila de la tabla. Si omitimos este paso,
        //va a saltar una excepción.

        if (generatedKeys.next()) {
            println("ID autoincremental generado por la BBDD es: ${generatedKeys.getLong(1)}")
        } else {
            println("No se ha generado una clave para el vehículo")
        }

        vehiculo = entity.copy(id = generatedKeys.getLong(1))

        //Cerramos conexión
        connection.close()
        //Comprobamos si se ha cerrado
        if (connection.isClosed) {
            println("¡Conexión cerrada con éxito!")
        } else {
            println("La conexión sigue abierta...")
        }

        return vehiculo
    }

    override fun delete(id: Long): Vehiculo? {
        logger.debug { "Eliminando vehículo" }

        //Creamos el vehículo (o nulo) que vamos a devolver en la función. Lo buscamos en la propia base de datos,
        // para saber si existe antes de intentar eliminarlo
        val vehiculo: Vehiculo? = getById(id)

        if (vehiculo != null) {
            //Abrimos conexión
            val connection = DriverManager.getConnection(h2databaseInMemoryURL)
            //Comprobamos si tenemos conexión
            if (connection == null) {
                println("NO se ha podido conectar")
            } else {
                println("¡Conexión establecida!")
            }

            //Creamos el borrado en SQL
            val sql = """DELETE FROM vehiculos WHERE id = ?"""

            //Creamos el statement
            val statement = connection.prepareStatement(sql)

            //Asignamos el valor al parámetro (?). Al solo haber 1, es el índice 1 sí o sí
            statement.setLong(1, id)

            //Ejecutamos el statement, tomará el valor del número de filas de la tabla a las que afecte la consulta
            val resultSet = statement.executeUpdate()

            if (resultSet == 1) {
                println("Vehículo con id $id borrado con éxito")
            }

            //Cerramos conexión
            connection.close()
            //Comprobamos si se ha cerrado
            if (connection.isClosed) {
                println("¡Conexión cerrada con éxito!")
            } else {
                println("La conexión sigue abierta...")
            }

        } else {
            println("No se ha podido borrar el vehículo con id $id porque no existe")
        }

        return vehiculo
    }

    override fun update(id: Long, entity: Vehiculo): Vehiculo? {
        logger.debug { "Actualizando vehículo con id $id" }

        //Creamos el vehículo (o nulo) que vamos a devolver en la función. Lo buscamos en la propia base de datos,
        // para saber si existe antes de intentar eliminarlo
        var vehiculo: Vehiculo? = getById(id)

        if (vehiculo != null) {

            //Fecha del día de hoy
            val timeStamp = LocalDate.now()

            //Abrimos conexión
            val connection = DriverManager.getConnection(h2databaseInMemoryURL)
            //Comprobamos si tenemos conexión
            if (connection == null) {
                println("NO se ha podido conectar")
            } else {
                println("¡Conexión establecida!")
            }

            //Creamos el update en SQL
            val sql = """UPDATE vehiculos
                |SET matricula = ?,
                |marca = ?,
                |modelo = ?,
                |fechaMatriculacion = ?,
                |permisoActivo = ?,
                |tipo = ?
                |WHERE id = ?
            """.trimMargin()

            //Preparamos el statement
            val statement = connection.prepareStatement(sql)

            //Asignamos el valor a actualizar, en este caso nuetra función update va a cambiar la fecha
            //de matriculación al día actual
            statement.setString(1, entity.matricula)
            statement.setString(2, entity.marca)
            statement.setString(3, entity.modelo)
            statement.setString(4, timeStamp.toString())
            statement.setBoolean(5, entity.permisoActivo)
            statement.setString(6, entity.tipo.toString())
            statement.setLong(7, id)

            //Ejecutamos el statement
            val result = statement.executeUpdate()

            if (result == 1) {
                println("¡Vehículo con id $id actualizado con éxito!")
            }

            vehiculo = vehiculo.copy(
                fechaMatriculacion = timeStamp
            )

            //Cerramos conexión
            connection.close()
            //Comprobamos si se ha cerrado
            if (connection.isClosed) {
                println("¡Conexión cerrada con éxito!")
            } else {
                println("La conexión sigue abierta...")
            }

        } else {
            println("No se ha podido actualizar el vehículo con id $id porque no existe")
        }

        return vehiculo
    }

    override fun getAll(): List<Vehiculo> {
        logger.debug { "Obteniendo todos los vehículos de la base de datos" }

        //Creamos la lista que vamos a devolver
        val concesionario = mutableListOf<Vehiculo>()

        //Abrimos conexión
        val connection = DriverManager.getConnection(h2databaseInMemoryURL)
        //Comprobamos si tenemos conexión
        if (connection == null) {
            println("NO se ha podido conectar")
        } else {
            println("¡Conexión establecida!")
        }

        //Creamos la consulta en SQL
        val sql = """SELECT * FROM vehiculos""".trimIndent()

        //Creamos el statement, en este caso no hay que pasarle parámetros
        val statement = connection.prepareStatement(sql)

        //Creamos y ejecutamos directamente el statement. En este caso, como la consulta no tiene parámetros (?)
        // No hace falta el prepareStatement()
        val resultSet = connection.createStatement().executeQuery(sql)


        //Con .next(), nos posicionamos en la primera fila de la tabla que devuelve la ejecución de la consulta,
        //los índices simbolizan la columna de dicha fila donde se encuentra el campo cuyo valor queremos obtener
        //para parsearlo al tipo de dato de nuestro modelo
        while (resultSet.next()) {
            var vehiculo = Vehiculo(
                id = resultSet.getLong(1),
                matricula = resultSet.getString(2),
                marca = resultSet.getString(3),
                modelo = resultSet.getString(4),
                fechaMatriculacion = LocalDate.parse(resultSet.getString(5)),
                permisoActivo = resultSet.getBoolean(6),
                tipo = Vehiculo.Tipo.valueOf(resultSet.getString(7))
            )

            concesionario.add(vehiculo)
        }

        //Cerramos conexión
        connection.close()
        //Comprobamos si se ha cerrado
        if (connection.isClosed) {
            println("¡Conexión cerrada con éxito!")
        } else {
            println("La conexión sigue abierta...")
        }

        return concesionario
    }

    override fun getById(id: Long): Vehiculo? {
        logger.debug { "Obteniendo vehículo con id: $id" }

        //Creamos el vehiculo que vamos a devolver
        var vehiculo: Vehiculo? = null

        //Abrimos conexión
        val connection = DriverManager.getConnection(h2databaseInMemoryURL)
        //Comprobamos si tenemos conexión
        if (connection == null) {
            println("NO se ha podido conectar")
        } else {
            println("¡Conexión establecida!")
        }

        //Creamos la consulta en SQL
        var sql = """SELECT * FROM vehiculos WHERE id = ?""".trimMargin()

        //Preparamos el statement
        val statement = connection.prepareStatement(sql)

        //Le asignamos al primer parámetro de la cadena SQL el valor del id que le entra como parámetro a la función
        //getById(id: Long)
        statement.setLong(1, id)

        //Ejecutamos el statement
        val resultSet = statement.executeQuery()

        //Vamos recorriendo las filas del resultado de la consulta con next, mientras que los índices son las columnas
        //donde se encuentra la info de cada campo, rescatamos el valor correspondiente y lo parseamos a los tipos de datos
        //de nuestro modelo de vehículo
        if (resultSet.next()) {
            vehiculo = Vehiculo(
                id = resultSet.getLong(1),
                matricula = resultSet.getString(2),
                marca = resultSet.getString(3),
                modelo = resultSet.getString(4),
                fechaMatriculacion = LocalDate.parse(resultSet.getString(5)),
                permisoActivo = resultSet.getBoolean(6),
                tipo = Vehiculo.Tipo.valueOf(resultSet.getString(7))
            )
            println("¡Vehículo con id $id encontrado con éxito!")
        } else {
            println("Vehículo con id $id no encontrado.")
        }

        //Cerramos conexión
        connection.close()
        //Comprobamos si se ha cerrado
        if (connection.isClosed) {
            println("¡Conexión cerrada con éxito!")
        } else {
            println("La conexión sigue abierta...")
        }

        return vehiculo
    }

}