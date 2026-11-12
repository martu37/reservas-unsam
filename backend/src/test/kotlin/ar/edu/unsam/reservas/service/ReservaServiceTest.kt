package ar.edu.unsam.reservas.service

import ar.edu.unsam.reservas.domain.EstadoReserva
import ar.edu.unsam.reservas.domain.EstadoTurno
import ar.edu.unsam.reservas.domain.NotFoundException
import ar.edu.unsam.reservas.domain.TipoEvento
import ar.edu.unsam.reservas.domain.TurnoNoDisponibleException
import ar.edu.unsam.reservas.repository.EspacioRepository
import ar.edu.unsam.reservas.repository.UsuarioRepository
import ar.edu.unsam.reservas.repository.postgres.DIA_DE_PRUEBA
import ar.edu.unsam.reservas.repository.postgres.JUAN
import ar.edu.unsam.reservas.repository.postgres.LUCIA
import ar.edu.unsam.reservas.repository.postgres.conMail
import ar.edu.unsam.reservas.repository.postgres.franjaDePrueba
import ar.edu.unsam.reservas.repository.postgres.llamado
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@DisplayName("Dado el service de reservas")
class ReservaServiceTest(
    @Autowired val reservaService: ReservaService,
    @Autowired val espacioService: EspacioService,
    @Autowired val historial: HistorialService,
    @Autowired val usuarios: UsuarioRepository,
    @Autowired val espacios: EspacioRepository,
) {
    private val juan get() = usuarios.conMail(JUAN).id
    private val lucia get() = usuarios.conMail(LUCIA).id
    private val cancha1 get() = espacios.llamado("Cancha 1").id

    @Test
    fun `reservar un turno libre lo confirma y registra el evento en el historial`() {
        val reserva = reservaService.reservar(juan, cancha1, franjaDePrueba("10:00", "11:00"))

        reserva.estado shouldBe EstadoReserva.CONFIRMADA
        val evento = historial.deUsuario(juan).first()
        evento.tipo shouldBe TipoEvento.RESERVA_CREADA
        evento.detalle["espacio"] shouldBe "Cancha 1"
    }

    @Test
    fun `un turno ocupado se rechaza, no se guarda y el intento queda en el historial`() {
        reservaService.reservar(juan, cancha1, franjaDePrueba("10:00", "11:00"))
        val antes = reservaService.deUsuario(lucia).size

        val error = shouldThrow<TurnoNoDisponibleException> {
            reservaService.reservar(lucia, cancha1, franjaDePrueba("10:00", "11:00"))
        }

        error.message shouldBe "El turno ya está reservado"
        reservaService.deUsuario(lucia) shouldHaveSize antes
        val evento = historial.deUsuario(lucia).first()
        evento.tipo shouldBe TipoEvento.RESERVA_RECHAZADA
        evento.detalle["motivo"] shouldBe "El turno ya está reservado"
    }

    @Test
    fun `una persona no puede reservar dos espacios a la misma hora y el intento queda en el historial`() {
        reservaService.reservar(juan, cancha1, franjaDePrueba("10:00", "11:00"))

        val error = shouldThrow<TurnoNoDisponibleException> {
            reservaService.reservar(juan, espacios.llamado("Cancha 2").id, franjaDePrueba("10:00", "11:00"))
        }

        error.message shouldBe "Ya tenés una reserva en ese horario"
        val evento = historial.deUsuario(juan).first()
        evento.tipo shouldBe TipoEvento.RESERVA_RECHAZADA
        evento.detalle["espacio"] shouldBe "Cancha 2"
    }

    @Test
    fun `no se puede reservar un espacio que no existe`() {
        shouldThrow<NotFoundException> {
            reservaService.reservar(juan, 9999, franjaDePrueba("10:00", "11:00"))
        }
    }

    @Test
    fun `cancelar deja la reserva cancelada y registra con cuánta anticipación`() {
        val reserva = reservaService.reservar(juan, cancha1, franjaDePrueba("10:00", "11:00"))

        val cancelada = reservaService.cancelar(reserva.id, juan)

        cancelada.estado shouldBe EstadoReserva.CANCELADA
        val evento = historial.deUsuario(juan).first()
        evento.tipo shouldBe TipoEvento.RESERVA_CANCELADA
        evento.detalle["anticipacionHoras"] shouldNotBe null
    }

    @Test
    fun `nadie puede cancelar la reserva de otra persona`() {
        val reserva = reservaService.reservar(juan, cancha1, franjaDePrueba("10:00", "11:00"))

        shouldThrow<NotFoundException> { reservaService.cancelar(reserva.id, lucia) }
    }

    @Test
    fun `la grilla del día muestra el turno reservado y el resto libre`() {
        reservaService.reservar(juan, cancha1, franjaDePrueba("10:00", "11:00"))

        val turnos = espacioService.turnos(cancha1, DIA_DE_PRUEBA)

        turnos shouldHaveSize 13
        turnos.first { it.franja == franjaDePrueba("10:00", "11:00") }.estado shouldBe EstadoTurno.RESERVADO
        turnos.first { it.franja == franjaDePrueba("11:00", "12:00") }.estado shouldBe EstadoTurno.LIBRE
    }
}
