package ar.edu.unsam.reservas.service

import ar.edu.unsam.reservas.domain.EventoHistorial
import ar.edu.unsam.reservas.domain.Franja
import ar.edu.unsam.reservas.domain.NotFoundException
import ar.edu.unsam.reservas.domain.Reserva
import ar.edu.unsam.reservas.domain.TurnoNoDisponibleException
import ar.edu.unsam.reservas.repository.BloqueoRepository
import ar.edu.unsam.reservas.repository.ReservaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock

@Service
class ReservaService(
    private val reservas: ReservaRepository,
    private val bloqueos: BloqueoRepository,
    private val usuarioService: UsuarioService,
    private val espacioService: EspacioService,
    private val historial: HistorialService,
    private val transaccion: TransactionTemplate,
    private val reloj: Clock,
) {
    fun deUsuario(usuarioId: Long): List<Reserva> {
        usuarioService.porId(usuarioId)
        return reservas.findByUsuario(usuarioId)
    }

    fun reservar(usuarioId: Long, espacioId: Long, franja: Franja): Reserva {
        val usuario = usuarioService.porId(usuarioId)
        val espacio = espacioService.porId(espacioId)
        val ahora = reloj.ahora()

        val reserva = try {
            transaccion.execute {
                val nueva = espacio.reservar(
                    usuario,
                    franja,
                    reservas.findByEspacioYDia(espacioId, franja.inicio.toLocalDate()),
                    bloqueos.findSuperpuestos(espacioId, franja),
                    ahora,
                )
                // Si otra persona reservó el mismo turno entre la consulta y este
                // INSERT, lo frena el EXCLUDE de la base y llega acá la misma excepción
                reservas.create(nueva)
            }!!
        } catch (rechazo: TurnoNoDisponibleException) {
            // El pedido rechazado no se guarda como reserva: solo queda en el historial
            historial.registrar(EventoHistorial.rechazada(usuario, espacio, franja, rechazo.message!!, ahora))
            throw rechazo
        }

        // Primero se confirmó en PostgreSQL; recién ahora se registra el evento
        historial.registrar(EventoHistorial.creada(reserva))
        return reserva
    }

    fun cancelar(reservaId: Long, usuarioId: Long): Reserva {
        val usuario = usuarioService.porId(usuarioId)
        val ahora = reloj.ahora()

        val reserva = transaccion.execute {
            val reserva = reservas.findById(reservaId)
                ?: throw NotFoundException("No se encontró la reserva")
            reserva.cancelar(usuario, ahora)
            reservas.update(reserva)
            reserva
        }!!

        historial.registrar(EventoHistorial.cancelada(reserva, ahora))
        return reserva
    }
}
