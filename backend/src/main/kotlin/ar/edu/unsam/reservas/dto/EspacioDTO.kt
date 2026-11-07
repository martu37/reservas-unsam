package ar.edu.unsam.reservas.dto

import ar.edu.unsam.reservas.domain.Espacio
import ar.edu.unsam.reservas.domain.TipoEspacio
import ar.edu.unsam.reservas.domain.Turno
import java.time.LocalDateTime
import java.time.LocalTime

data class TipoEspacioDTO(
    val id: Long,
    val nombre: String,
    val duracionMin: Int,
    val horaApertura: LocalTime,
    val horaCierre: LocalTime,
)

// El espacio viaja con su tipo adentro: es el JOIN espacio-tipo_espacio
data class EspacioDTO(
    val id: Long,
    val nombre: String,
    val tipo: TipoEspacioDTO,
    val ubicacion: String,
    val activo: Boolean,
)

data class TurnoDTO(
    val inicio: LocalDateTime,
    val fin: LocalDateTime,
    val estado: String,
)

fun TipoEspacio.toDTO() = TipoEspacioDTO(
    id = id,
    nombre = nombre,
    duracionMin = duracionMin,
    horaApertura = horaApertura,
    horaCierre = horaCierre,
)

fun Espacio.toDTO() = EspacioDTO(
    id = id,
    nombre = nombre,
    tipo = tipo.toDTO(),
    ubicacion = ubicacion,
    activo = activo,
)

fun Turno.toDTO() = TurnoDTO(
    inicio = franja.inicio,
    fin = franja.fin,
    estado = estado.paraElFront(),
)
