package ar.edu.unsam.reservas.domain

import java.time.LocalDate
import java.time.LocalTime

class TipoEspacio {
    var id: Long = 0
    var nombre: String = ""
    var duracionMin: Int = 60
    var horaApertura: LocalTime = LocalTime.of(9, 0)
    var horaCierre: LocalTime = LocalTime.of(22, 0)

    // Los turnos no se guardan en la base: salen del horario y la duración del tipo
    fun turnosDelDia(fecha: LocalDate): List<Franja> {
        val cierre = fecha.atTime(horaCierre)
        return generateSequence(fecha.atTime(horaApertura)) { it.plusMinutes(duracionMin.toLong()) }
            .map { inicio -> Franja(inicio, inicio.plusMinutes(duracionMin.toLong())) }
            .takeWhile { !it.fin.isAfter(cierre) }
            .toList()
    }

    fun esTurnoValido(franja: Franja) = franja in turnosDelDia(franja.inicio.toLocalDate())

    fun validate() {
        if (nombre.isBlank()) {
            throw BusinessException("El tipo de espacio necesita un nombre")
        }
        if (duracionMin <= 0) {
            throw BusinessException("La duración del turno tiene que ser mayor a cero")
        }
        if (!horaCierre.isAfter(horaApertura)) {
            throw BusinessException("El horario de cierre tiene que ser posterior a la apertura")
        }
    }
}
