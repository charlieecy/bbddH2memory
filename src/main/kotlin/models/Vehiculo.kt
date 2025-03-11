package org.example.models

import java.time.LocalDate

data class Vehiculo (
    var id: Long = NEW_ID,
    val matricula: String,
    val marca: String,
    val modelo: String,
    var fechaMatriculacion: LocalDate,
    val permisoActivo: Boolean,
    val tipo: Tipo
) {
    companion object {
        const val NEW_ID: Long = -1
    }

    enum class Tipo {
        ELECTRICO, HIBRIDO, COMBUSTION
    }

    override fun toString(): String {
        return "Vehiculo (id = $id, matrícula = $matricula, marca = $marca, modelo = $modelo, fechaMatriculacion = $fechaMatriculacion, permisoActivo = $permisoActivo, tipo = $tipo)"
    }

}