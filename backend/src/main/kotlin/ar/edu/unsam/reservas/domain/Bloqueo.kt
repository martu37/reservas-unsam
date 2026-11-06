package ar.edu.unsam.reservas.domain

import java.time.LocalDateTime

// Un espacio fuera de servicio por mantenimiento: mientras dure no se puede reservar
class Bloqueo(
    var espacio: Espacio,
    var franja: Franja,
    var motivo: String,
    var creadoPor: Usuario,
    var creadoEn: LocalDateTime,
) {
    var id: Long = 0

    val inicio get() = franja.inicio
    val fin get() = franja.fin

    fun cubre(otra: Franja) = franja.sePisaCon(otra)

    fun yaTermino(ahora: LocalDateTime) = franja.yaTermino(ahora)

    fun actualizar(espacio: Espacio, franja: Franja, motivo: String) {
        this.espacio = espacio
        this.franja = franja
        this.motivo = motivo
        validate()
    }

    fun validate() {
        if (motivo.isBlank()) {
            throw BusinessException("El bloqueo necesita un motivo")
        }
        if (!creadoPor.esAdmin()) {
            throw BusinessException("Solo un administrador puede bloquear espacios")
        }
    }
}
