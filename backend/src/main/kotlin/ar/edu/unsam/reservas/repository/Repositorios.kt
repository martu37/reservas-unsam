package ar.edu.unsam.reservas.repository

import ar.edu.unsam.reservas.domain.Bloqueo
import ar.edu.unsam.reservas.domain.Espacio
import ar.edu.unsam.reservas.domain.EventoHistorial
import ar.edu.unsam.reservas.domain.Franja
import ar.edu.unsam.reservas.domain.Reserva
import ar.edu.unsam.reservas.domain.Usuario
import java.time.LocalDate
import java.time.LocalDateTime

// Lo que la aplicación necesita guardar y buscar, sin decir con qué motor.
// Los services dependen de estas interfaces; las implementaciones están en
// repository/postgres (y el historial va a tener además una en MongoDB).

interface UsuarioRepository {
    fun findAll(): List<Usuario>
    fun findById(id: Long): Usuario?
}

interface EspacioRepository {
    fun findAll(): List<Espacio>
    fun findById(id: Long): Espacio?
}

interface ReservaRepository {
    fun findById(id: Long): Reserva?
    fun findByUsuario(usuarioId: Long): List<Reserva>
    fun findByEspacioYDia(espacioId: Long, dia: LocalDate): List<Reserva>

    // Las confirmadas que todavía no empezaron y se pisan con la franja:
    // son las que hay que cancelar cuando se bloquea el espacio
    fun findConfirmadasSuperpuestas(espacioId: Long, franja: Franja, desde: LocalDateTime): List<Reserva>

    fun create(reserva: Reserva): Reserva
    fun update(reserva: Reserva)
}

// El repositorio polimórfico del punto 6: una implementación en MongoDB
// (la principal) y otra en PostgreSQL (el respaldo)
interface HistorialRepository {
    // "mongo" o "postgres": el front muestra qué motor contestó
    val motor: String

    fun registrar(evento: EventoHistorial)
    fun findByUsuario(usuarioId: Long): List<EventoHistorial>
}

interface BloqueoRepository {
    fun findAll(): List<Bloqueo>
    fun findById(id: Long): Bloqueo?
    fun findSuperpuestos(espacioId: Long, franja: Franja): List<Bloqueo>
    fun create(bloqueo: Bloqueo): Bloqueo
    fun update(bloqueo: Bloqueo)
    fun delete(id: Long): Boolean
}
