package ar.edu.unsam.reservas.domain

import java.time.LocalDateTime

enum class TipoEvento(val descripcion: String) {
    RESERVA_CREADA("Reserva confirmada"),
    RESERVA_CANCELADA("Reserva cancelada"),
    RESERVA_RECHAZADA("Pedido rechazado"),
}

// Lo que pasó con los pedidos de un usuario. Se guarda en MongoDB: el detalle
// cambia según el tipo de evento y cada documento guarda solo lo que le corresponde.
class EventoHistorial(
    var usuarioId: Long,
    var tipo: TipoEvento,
    var fecha: LocalDateTime,
    var detalle: Map<String, Any?>,
) {
    var id: String? = null

    companion object {
        fun creada(reserva: Reserva) = EventoHistorial(
            reserva.usuario.id,
            TipoEvento.RESERVA_CREADA,
            reserva.creadaEn,
            detalleDe(reserva.espacio, reserva.franja) + ("reservaId" to reserva.id),
        )

        fun cancelada(reserva: Reserva, ahora: LocalDateTime) = EventoHistorial(
            reserva.usuario.id,
            TipoEvento.RESERVA_CANCELADA,
            ahora,
            detalleDe(reserva.espacio, reserva.franja) + mapOf(
                "reservaId" to reserva.id,
                "anticipacionHoras" to reserva.horasDeAnticipacion(ahora),
            ),
        )

        // La canceló el sistema porque un admin bloqueó el espacio
        fun canceladaPorMantenimiento(reserva: Reserva, motivo: String, ahora: LocalDateTime) = EventoHistorial(
            reserva.usuario.id,
            TipoEvento.RESERVA_CANCELADA,
            ahora,
            detalleDe(reserva.espacio, reserva.franja) + mapOf(
                "reservaId" to reserva.id,
                "motivo" to "Mantenimiento: $motivo",
            ),
        )

        fun rechazada(
            usuario: Usuario,
            espacio: Espacio,
            franja: Franja,
            motivo: String,
            ahora: LocalDateTime,
        ) = EventoHistorial(
            usuario.id,
            TipoEvento.RESERVA_RECHAZADA,
            ahora,
            detalleDe(espacio, franja) + ("motivo" to motivo),
        )

        private fun detalleDe(espacio: Espacio, franja: Franja) = mapOf(
            "espacio" to espacio.nombre,
            "inicio" to franja.inicio,
            "fin" to franja.fin,
        )
    }
}
