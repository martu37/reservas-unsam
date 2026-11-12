package ar.edu.unsam.reservas.service

import ar.edu.unsam.reservas.domain.Espacio
import ar.edu.unsam.reservas.domain.Franja
import ar.edu.unsam.reservas.domain.NotFoundException
import ar.edu.unsam.reservas.domain.Turno
import ar.edu.unsam.reservas.domain.UsoEspacio
import ar.edu.unsam.reservas.repository.BloqueoRepository
import ar.edu.unsam.reservas.repository.EspacioRepository
import ar.edu.unsam.reservas.repository.ReservaRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate

@Service
class EspacioService(
    private val espacios: EspacioRepository,
    private val reservas: ReservaRepository,
    private val bloqueos: BloqueoRepository,
    private val reloj: Clock,
) {
    fun todos(): List<Espacio> = espacios.findAll()

    fun uso(): List<UsoEspacio> = reservas.usoPorEspacio()

    fun porId(id: Long): Espacio =
        espacios.findById(id) ?: throw NotFoundException("No se encontró el espacio $id")

    // La grilla del día: el service junta los datos y el dominio decide el estado
    fun turnos(espacioId: Long, dia: LocalDate): List<Turno> {
        val espacio = porId(espacioId)
        val todoElDia = Franja(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay())
        return espacio.turnosPara(
            dia,
            reservas.findByEspacioYDia(espacioId, dia),
            bloqueos.findSuperpuestos(espacioId, todoElDia),
            reloj.ahora(),
        )
    }
}
