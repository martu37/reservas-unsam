package ar.edu.unsam.reservas.domain

import java.time.LocalDateTime

// Un intervalo [inicio, fin). Lo usan las reservas, los bloqueos y los turnos.
data class Franja(val inicio: LocalDateTime, val fin: LocalDateTime) {
    init {
        if (!fin.isAfter(inicio)) {
            throw BusinessException("El fin tiene que ser posterior al inicio")
        }
    }

    // Se pisan si una empieza antes de que termine la otra. Dos turnos seguidos
    // (18 a 19 y 19 a 20) no se pisan.
    fun sePisaCon(otra: Franja) = inicio < otra.fin && otra.inicio < fin

    fun yaEmpezo(ahora: LocalDateTime) = !inicio.isAfter(ahora)

    fun yaTermino(ahora: LocalDateTime) = !fin.isAfter(ahora)
}
