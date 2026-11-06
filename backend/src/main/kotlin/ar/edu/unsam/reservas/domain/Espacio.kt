package ar.edu.unsam.reservas.domain

import java.time.LocalDate
import java.time.LocalDateTime

class Espacio {
    var id: Long = 0
    var nombre: String = ""
    var tipo: TipoEspacio = TipoEspacio()
    var ubicacion: String = ""
    var activo: Boolean = true

    // Arma la grilla del día: cada turno del tipo con su estado
    fun turnosPara(
        fecha: LocalDate,
        reservas: List<Reserva>,
        bloqueos: List<Bloqueo>,
        ahora: LocalDateTime,
    ): List<Turno> = tipo.turnosDelDia(fecha).map { franja ->
        val estado = when {
            bloqueos.any { it.cubre(franja) } -> EstadoTurno.BLOQUEADO
            reservas.any { it.ocupa(franja) } -> EstadoTurno.RESERVADO
            franja.yaEmpezo(ahora) -> EstadoTurno.PASADO
            else -> EstadoTurno.LIBRE
        }
        Turno(franja, estado)
    }

    // Recibe las reservas y bloqueos que ya existen en ese horario.
    // La verificación de superposición acá es para dar un mensaje claro: la que
    // garantiza que nunca haya dos reservas pisadas es la restricción de PostgreSQL,
    // porque dos personas pueden pasar este chequeo al mismo tiempo.
    fun reservar(
        usuario: Usuario,
        franja: Franja,
        reservas: List<Reserva>,
        bloqueos: List<Bloqueo>,
        ahora: LocalDateTime,
    ): Reserva {
        if (!activo) {
            throw BusinessException("El espacio no está disponible")
        }
        if (!usuario.activo) {
            throw BusinessException("El usuario no puede hacer reservas")
        }
        if (!tipo.esTurnoValido(franja)) {
            throw BusinessException("El horario no corresponde a un turno")
        }
        if (franja.yaEmpezo(ahora)) {
            throw BusinessException("El turno ya pasó")
        }
        bloqueos.find { it.cubre(franja) }?.let {
            throw TurnoNoDisponibleException("El espacio está bloqueado: ${it.motivo}")
        }
        if (reservas.any { it.ocupa(franja) }) {
            throw TurnoNoDisponibleException("El turno ya está reservado")
        }
        return Reserva(usuario, this, franja, ahora)
    }

    fun validate() {
        if (nombre.isBlank()) {
            throw BusinessException("El espacio necesita un nombre")
        }
        tipo.validate()
    }
}
