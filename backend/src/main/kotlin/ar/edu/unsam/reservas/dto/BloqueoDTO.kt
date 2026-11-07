package ar.edu.unsam.reservas.dto

import ar.edu.unsam.reservas.domain.Bloqueo
import ar.edu.unsam.reservas.domain.Franja
import java.time.LocalDateTime

data class BloqueoDTO(
    val id: Long,
    val espacioId: Long,
    val espacioNombre: String,
    val inicio: LocalDateTime,
    val fin: LocalDateTime,
    val motivo: String,
    val creadoPor: Long,
    val creadoEn: LocalDateTime,
)

// Lo que carga el admin en el formulario (BloqueoData en el front)
data class BloqueoDataDTO(
    val espacioId: Long,
    val inicio: LocalDateTime,
    val fin: LocalDateTime,
    val motivo: String,
) {
    fun franja() = Franja(inicio, fin)
}

fun Bloqueo.toDTO() = BloqueoDTO(
    id = id,
    espacioId = espacio.id,
    espacioNombre = espacio.nombre,
    inicio = inicio,
    fin = fin,
    motivo = motivo,
    creadoPor = creadoPor.id,
    creadoEn = creadoEn,
)
