package ar.edu.unsam.reservas.domain

enum class EstadoTurno(val descripcion: String) {
    LIBRE("Libre"),
    RESERVADO("Reservado"),
    BLOQUEADO("En mantenimiento"),
    PASADO("Ya pasó"),
}

data class Turno(val franja: Franja, val estado: EstadoTurno)
