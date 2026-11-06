package ar.edu.unsam.reservas.domain

import java.time.Duration
import java.time.LocalDateTime

enum class EstadoReserva(val descripcion: String) {
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada"),
}

class Reserva(
    var usuario: Usuario,
    var espacio: Espacio,
    var franja: Franja,
    var creadaEn: LocalDateTime,
) {
    var id: Long = 0
    var estado: EstadoReserva = EstadoReserva.CONFIRMADA
    var canceladaEn: LocalDateTime? = null

    val inicio get() = franja.inicio
    val fin get() = franja.fin

    fun estaConfirmada() = estado == EstadoReserva.CONFIRMADA

    fun esDe(otro: Usuario) = usuario.id == otro.id

    // Una reserva cancelada libera el turno, así que no ocupa nada
    fun ocupa(otra: Franja) = estaConfirmada() && franja.sePisaCon(otra)

    fun puedeCancelarse(ahora: LocalDateTime) = estaConfirmada() && !franja.yaEmpezo(ahora)

    fun cancelar(quien: Usuario, ahora: LocalDateTime) {
        if (!esDe(quien)) {
            throw NotFoundException("No se encontró la reserva")
        }
        if (!puedeCancelarse(ahora)) {
            throw BusinessException("La reserva ya no se puede cancelar")
        }
        estado = EstadoReserva.CANCELADA
        canceladaEn = ahora
    }

    fun horasDeAnticipacion(ahora: LocalDateTime) = Duration.between(ahora, inicio).toHours()
}
