package ar.edu.unsam.reservas.dto

import ar.edu.unsam.reservas.domain.Franja
import ar.edu.unsam.reservas.domain.Reserva
import java.time.LocalDateTime

// Lo que se devuelve: no manda el usuario ni el espacio enteros, solo los ids
// y el nombre del espacio para mostrarlo (como la vista reserva_detalle)
data class ReservaDTO(
    val id: Long,
    val usuarioId: Long,
    val espacioId: Long,
    val espacioNombre: String,
    val inicio: LocalDateTime,
    val fin: LocalDateTime,
    val estado: String,
    val creadaEn: LocalDateTime,
    val canceladaEn: LocalDateTime?,
)

// Lo que manda el front para reservar: qué espacio y qué turno.
// Quién reserva va aparte, como el usuario que hace el pedido.
data class NuevaReservaDTO(
    val espacioId: Long,
    val inicio: LocalDateTime,
    val fin: LocalDateTime,
) {
    fun franja() = Franja(inicio, fin)
}

fun Reserva.toDTO() = ReservaDTO(
    id = id,
    usuarioId = usuario.id,
    espacioId = espacio.id,
    espacioNombre = espacio.nombre,
    inicio = inicio,
    fin = fin,
    estado = estado.paraElFront(),
    creadaEn = creadaEn,
    canceladaEn = canceladaEn,
)
