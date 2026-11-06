package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.EstadoReserva
import ar.edu.unsam.reservas.domain.TurnoNoDisponibleException
import ar.edu.unsam.reservas.repository.BloqueoRepository
import ar.edu.unsam.reservas.repository.EspacioRepository
import ar.edu.unsam.reservas.repository.ReservaRepository
import ar.edu.unsam.reservas.repository.UsuarioRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

// Corre contra el PostgreSQL de docker-compose. Cada test se deshace al terminar.
@SpringBootTest
@Transactional
@DisplayName("Dado el repositorio de reservas en PostgreSQL")
class ReservaRepositoryPostgresTest(
    @Autowired val reservas: ReservaRepository,
    @Autowired val espacios: EspacioRepository,
    @Autowired val usuarios: UsuarioRepository,
    @Autowired val bloqueos: BloqueoRepository,
) {
    private val juan get() = usuarios.conMail(JUAN)
    private val lucia get() = usuarios.conMail(LUCIA)
    private val cancha1 get() = espacios.llamado("Cancha 1")

    @Test
    fun `lee las reservas de un usuario con su espacio y el tipo del espacio`() {
        val deJuan = reservas.findByUsuario(juan.id)

        deJuan shouldHaveSize 4
        deJuan.map { it.inicio } shouldBe deJuan.map { it.inicio }.sorted()
        deJuan.first().espacio.tipo.nombre shouldBe "Cancha fútbol 5"
        deJuan.first().usuario.nombreCompleto shouldBe "Juan Pérez"
    }

    @Test
    fun `guarda una reserva con el id que genera la base`() {
        val guardada = reservas.create(reservaDePrueba(juan, cancha1, franjaDePrueba("10:00", "11:00")))

        guardada.id shouldBeGreaterThan 0
        val leida = reservas.findById(guardada.id)!!
        leida.espacio.nombre shouldBe "Cancha 1"
        leida.franja shouldBe franjaDePrueba("10:00", "11:00")
        leida.estado shouldBe EstadoReserva.CONFIRMADA
    }

    @Test
    fun `la base rechaza una reserva superpuesta aunque nadie la haya chequeado antes`() {
        reservas.create(reservaDePrueba(juan, cancha1, franjaDePrueba("10:00", "11:00")))

        val error = shouldThrow<TurnoNoDisponibleException> {
            reservas.create(reservaDePrueba(lucia, cancha1, franjaDePrueba("10:00", "11:00")))
        }
        error.message shouldBe "El turno ya está reservado"
    }

    @Test
    fun `la base rechaza una reserva en un espacio bloqueado`() {
        bloqueos.create(bloqueoDePrueba(cancha1, franjaDePrueba("09:00", "13:00"), usuarios.conMail(DANIELA)))

        val error = shouldThrow<TurnoNoDisponibleException> {
            reservas.create(reservaDePrueba(juan, cancha1, franjaDePrueba("10:00", "11:00")))
        }
        error.message shouldBe "El espacio está bloqueado: Corte de pasto"
    }

    @Test
    fun `una reserva cancelada libera el turno`() {
        val reserva = reservas.create(reservaDePrueba(juan, cancha1, franjaDePrueba("10:00", "11:00")))

        reserva.cancelar(juan, LocalDateTime.now())
        reservas.update(reserva)
        val otra = reservas.create(reservaDePrueba(lucia, cancha1, franjaDePrueba("10:00", "11:00")))

        reservas.findById(reserva.id)!!.estado shouldBe EstadoReserva.CANCELADA
        otra.estado shouldBe EstadoReserva.CONFIRMADA
    }

    @Test
    fun `busca las reservas de un espacio en un día`() {
        reservas.create(reservaDePrueba(juan, cancha1, franjaDePrueba("10:00", "11:00")))
        reservas.create(reservaDePrueba(lucia, cancha1, franjaDePrueba("15:00", "16:00")))
        reservas.create(reservaDePrueba(juan, cancha1, franjaDePrueba("10:00", "11:00", DIA_DE_PRUEBA.plusDays(1))))

        reservas.findByEspacioYDia(cancha1.id, DIA_DE_PRUEBA) shouldHaveSize 2
    }

    @Test
    fun `encuentra las confirmadas que se pisan con un horario`() {
        reservas.create(reservaDePrueba(juan, cancha1, franjaDePrueba("10:00", "11:00")))
        reservas.create(reservaDePrueba(lucia, cancha1, franjaDePrueba("11:00", "12:00")))
        reservas.create(reservaDePrueba(juan, cancha1, franjaDePrueba("14:00", "15:00")))

        val afectadas = reservas.findConfirmadasSuperpuestas(cancha1.id, franjaDePrueba("10:30", "12:00"), LocalDateTime.now())

        afectadas.map { it.usuario.mail } shouldBe listOf(JUAN, LUCIA)
    }
}
