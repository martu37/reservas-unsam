package ar.edu.unsam.reservas.dto

import ar.edu.unsam.reservas.domain.EventoHistorial
import java.time.LocalDateTime

data class EventoHistorialDTO(
    val id: String?,
    val usuarioId: Long,
    val tipo: String,
    val fecha: LocalDateTime,
    val detalle: Map<String, Any?>,
)

// La respuesta dice qué motor la contestó: el front lo muestra (punto 8 de la consigna)
data class HistorialDTO(
    val motor: String,
    val eventos: List<EventoHistorialDTO>,
)

fun EventoHistorial.toDTO() = EventoHistorialDTO(
    id = id,
    usuarioId = usuarioId,
    tipo = tipo.paraElFront(),
    fecha = fecha,
    detalle = detalle,
)
