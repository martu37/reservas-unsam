package ar.edu.unsam.reservas.service

import ar.edu.unsam.reservas.domain.BusinessException
import ar.edu.unsam.reservas.domain.EstadoReserva
import ar.edu.unsam.reservas.domain.EstadoTurno
import ar.edu.unsam.reservas.domain.NotFoundException
import ar.edu.unsam.reservas.domain.TipoEvento
import ar.edu.unsam.reservas.repository.EspacioRepository
import ar.edu.unsam.reservas.repository.ReservaRepository
import ar.edu.unsam.reservas.repository.UsuarioRepository
import ar.edu.unsam.reservas.repository.postgres.DANIELA
import ar.edu.unsam.reservas.repository.postgres.DIA_DE_PRUEBA
import ar.edu.unsam.reservas.repository.postgres.JUAN
import ar.edu.unsam.reservas.repository.postgres.LUCIA
import ar.edu.unsam.reservas.repository.postgres.conMail
import ar.edu.unsam.reservas.repository.postgres.franjaDePrueba
import ar.edu.unsam.reservas.repository.postgres.llamado
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@DisplayName("Dado el service de bloqueos")
class BloqueoServiceTest(
    @Autowired val bloqueoService: BloqueoService,
    @Autowired val reservaService: ReservaService,
    @Autowired val espacioService: EspacioService,
    @Autowired val historial: HistorialService,
    @Autowired val reservas: ReservaRepository,
    @Autowired val usuarios: UsuarioRepository,
    @Autowired val espacios: EspacioRepository,
) {
    private val juan get() = usuarios.conMail(JUAN).id
    private val lucia get() = usuarios.conMail(LUCIA).id
    private val daniela get() = usuarios.conMail(DANIELA).id
    private val cancha1 get() = espacios.llamado("Cancha 1").id

    private fun estado(reservaId: Long) = reservas.findById(reservaId)!!.estado

    @Test
    fun `bloquear cancela las reservas que caen adentro y le avisa a cada usuario`() {
        val deJuan = reservaService.reservar(juan, cancha1, franjaDePrueba("10:00", "11:00"))
        val deLucia = reservaService.reservar(lucia, cancha1, franjaDePrueba("11:00", "12:00"))
        val afuera = reservaService.reservar(juan, cancha1, franjaDePrueba("15:00", "16:00"))

        bloqueoService.crear(daniela, cancha1, franjaDePrueba("10:00", "13:00"), "Arco roto")

        estado(deJuan.id) shouldBe EstadoReserva.CANCELADA
        estado(deLucia.id) shouldBe EstadoReserva.CANCELADA
        estado(afuera.id) shouldBe EstadoReserva.CONFIRMADA
        val aviso = historial.deUsuario(lucia).first()
        aviso.tipo shouldBe TipoEvento.RESERVA_CANCELADA
        aviso.detalle["motivo"] shouldBe "Mantenimiento: Arco roto"
    }

    @Test
    fun `después de bloquear, esos turnos aparecen bloqueados en la grilla`() {
        bloqueoService.crear(daniela, cancha1, franjaDePrueba("10:00", "13:00"), "Arco roto")

        val turnos = espacioService.turnos(cancha1, DIA_DE_PRUEBA)

        turnos.first { it.franja == franjaDePrueba("12:00", "13:00") }.estado shouldBe EstadoTurno.BLOQUEADO
        turnos.first { it.franja == franjaDePrueba("13:00", "14:00") }.estado shouldBe EstadoTurno.LIBRE
    }

    @Test
    fun `solo un admin puede bloquear y, si no lo es, no se cancela nada`() {
        val reserva = reservaService.reservar(lucia, cancha1, franjaDePrueba("10:00", "11:00"))

        shouldThrow<BusinessException> {
            bloqueoService.crear(juan, cancha1, franjaDePrueba("10:00", "13:00"), "Arco roto")
        }
        estado(reserva.id) shouldBe EstadoReserva.CONFIRMADA
    }

    @Test
    fun `extender un bloqueo cancela las reservas que ahora quedan adentro`() {
        val bloqueo = bloqueoService.crear(daniela, cancha1, franjaDePrueba("10:00", "12:00"), "Arco roto")
        val reserva = reservaService.reservar(juan, cancha1, franjaDePrueba("12:00", "13:00"))

        bloqueoService.actualizar(bloqueo.id, daniela, cancha1, franjaDePrueba("10:00", "14:00"), "Arco roto")

        estado(reserva.id) shouldBe EstadoReserva.CANCELADA
        bloqueoService.porId(bloqueo.id).franja shouldBe franjaDePrueba("10:00", "14:00")
    }

    @Test
    fun `no se puede cargar un bloqueo que se pisa con otro del mismo espacio`() {
        bloqueoService.crear(daniela, cancha1, franjaDePrueba("10:00", "13:00"), "Arco roto")

        val error = shouldThrow<BusinessException> {
            bloqueoService.crear(daniela, cancha1, franjaDePrueba("12:00", "15:00"), "Pintura")
        }
        error.message shouldBe "Ya hay un bloqueo en ese horario"
    }

    @Test
    fun `borrar un bloqueo que no existe avisa que no se encontró`() {
        shouldThrow<NotFoundException> { bloqueoService.eliminar(9999, daniela) }
    }
}
