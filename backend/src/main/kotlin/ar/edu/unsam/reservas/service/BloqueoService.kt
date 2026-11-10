package ar.edu.unsam.reservas.service

import ar.edu.unsam.reservas.domain.Bloqueo
import ar.edu.unsam.reservas.domain.BusinessException
import ar.edu.unsam.reservas.domain.Espacio
import ar.edu.unsam.reservas.domain.EventoHistorial
import ar.edu.unsam.reservas.domain.Franja
import ar.edu.unsam.reservas.domain.NotFoundException
import ar.edu.unsam.reservas.domain.Reserva
import ar.edu.unsam.reservas.domain.Usuario
import ar.edu.unsam.reservas.repository.BloqueoRepository
import ar.edu.unsam.reservas.repository.ReservaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.LocalDateTime

@Service
class BloqueoService(
    private val bloqueos: BloqueoRepository,
    private val reservas: ReservaRepository,
    private val usuarioService: UsuarioService,
    private val espacioService: EspacioService,
    private val historial: HistorialService,
    private val transaccion: TransactionTemplate,
    private val reloj: Clock,
) {
    fun todos(): List<Bloqueo> = bloqueos.findAll()

    fun porId(id: Long): Bloqueo =
        bloqueos.findById(id) ?: throw NotFoundException("No se encontró el bloqueo")

    // Decisión del grupo: bloquear cancela las reservas futuras que caen adentro.
    // Cancelarlas y crear el bloqueo pasa en UNA transacción: o pasan las dos
    // cosas o ninguna. Nadie pierde su turno por un bloqueo que no se creó.
    fun crear(adminId: Long, espacioId: Long, franja: Franja, motivo: String): Bloqueo {
        val admin = exigirAdmin(adminId)
        val espacio = espacioService.porId(espacioId)
        val ahora = reloj.ahora()
        val bloqueo = Bloqueo(espacio, franja, motivo.trim(), admin, ahora).also { it.validate() }
        exigirLibreDeBloqueos(bloqueo)

        val canceladas = transaccion.execute {
            val afectadas = cancelarAfectadas(espacio, franja, ahora)
            bloqueos.create(bloqueo)
            afectadas
        }!!

        avisar(canceladas, bloqueo, ahora)
        return bloqueo
    }

    fun actualizar(id: Long, adminId: Long, espacioId: Long, franja: Franja, motivo: String): Bloqueo {
        exigirAdmin(adminId)
        val espacio = espacioService.porId(espacioId)
        val ahora = reloj.ahora()

        val (bloqueo, canceladas) = transaccion.execute {
            val bloqueo = porId(id)
            bloqueo.actualizar(espacio, franja, motivo.trim())
            exigirLibreDeBloqueos(bloqueo)
            val afectadas = cancelarAfectadas(espacio, franja, ahora)
            bloqueos.update(bloqueo)
            bloqueo to afectadas
        }!!

        avisar(canceladas, bloqueo, ahora)
        return bloqueo
    }

    fun eliminar(id: Long, adminId: Long) {
        exigirAdmin(adminId)
        if (!bloqueos.delete(id)) {
            throw NotFoundException("No se encontró el bloqueo")
        }
    }

    private fun cancelarAfectadas(espacio: Espacio, franja: Franja, ahora: LocalDateTime): List<Reserva> =
        reservas.findConfirmadasSuperpuestas(espacio.id, franja, ahora).onEach {
            it.cancelarPorMantenimiento(ahora)
            reservas.update(it)
        }

    // Después de confirmar en PostgreSQL: a cada usuario le queda en su
    // historial que su reserva se canceló y por qué
    private fun avisar(canceladas: List<Reserva>, bloqueo: Bloqueo, ahora: LocalDateTime) =
        historial.registrarTodos(canceladas.map { EventoHistorial.canceladaPorMantenimiento(it, bloqueo.motivo, ahora) })

    // Dos bloqueos del mismo espacio no se pisan: si hace falta más tiempo,
    // se edita el que ya existe
    private fun exigirLibreDeBloqueos(bloqueo: Bloqueo) {
        if (bloqueos.findSuperpuestos(bloqueo.espacio.id, bloqueo.franja).any { it.id != bloqueo.id }) {
            throw BusinessException("Ya hay un bloqueo en ese horario")
        }
    }

    private fun exigirAdmin(usuarioId: Long): Usuario =
        usuarioService.porId(usuarioId).also {
            if (!it.esAdmin()) throw BusinessException("Solo un administrador puede gestionar bloqueos")
        }
}
