package ar.edu.unsam.reservas.service

import ar.edu.unsam.reservas.domain.EventoHistorial
import ar.edu.unsam.reservas.repository.HistorialRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class HistorialService(private val historial: HistorialRepository) {

    private val log = LoggerFactory.getLogger(javaClass)

    // El historial nunca rompe una reserva: si no se puede guardar el evento,
    // la reserva ya quedó confirmada en PostgreSQL y solo se avisa en el log
    fun registrar(evento: EventoHistorial) {
        try {
            historial.registrar(evento)
        } catch (error: Exception) {
            log.warn("No se pudo registrar el evento ${evento.tipo} del usuario ${evento.usuarioId}", error)
        }
    }

    fun registrarTodos(eventos: List<EventoHistorial>) = eventos.forEach(::registrar)

    fun deUsuario(usuarioId: Long): List<EventoHistorial> = historial.findByUsuario(usuarioId)
}
