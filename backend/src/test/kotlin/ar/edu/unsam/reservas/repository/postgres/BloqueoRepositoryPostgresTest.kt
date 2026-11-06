package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.BusinessException
import ar.edu.unsam.reservas.domain.TurnoNoDisponibleException
import ar.edu.unsam.reservas.repository.BloqueoRepository
import ar.edu.unsam.reservas.repository.EspacioRepository
import ar.edu.unsam.reservas.repository.ReservaRepository
import ar.edu.unsam.reservas.repository.UsuarioRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@Transactional
@DisplayName("Dado el repositorio de bloqueos en PostgreSQL")
class BloqueoRepositoryPostgresTest(
    @Autowired val bloqueos: BloqueoRepository,
    @Autowired val reservas: ReservaRepository,
    @Autowired val espacios: EspacioRepository,
    @Autowired val usuarios: UsuarioRepository,
) {
    private val daniela get() = usuarios.conMail(DANIELA)
    private val cancha3 get() = espacios.llamado("Cancha 3")

    @Test
    fun `lee los bloqueos con su espacio y quién los cargó`() {
        val todos = bloqueos.findAll()

        todos shouldHaveSize 2
        todos.map { it.motivo }.toSet() shouldBe setOf("Cambio de rejilla", "Corte de pasto")
        todos.all { it.creadoPor.esAdmin() }.shouldBeTrue()
    }

    @Test
    fun `no deja bloquear un horario con reservas confirmadas sin cancelarlas antes`() {
        reservas.create(reservaDePrueba(usuarios.conMail(JUAN), cancha3, franjaDePrueba("09:00", "10:30")))

        val error = shouldThrow<TurnoNoDisponibleException> {
            bloqueos.create(bloqueoDePrueba(cancha3, franjaDePrueba("09:00", "13:00"), daniela))
        }
        error.message!! shouldStartWith "Hay reservas confirmadas en ese horario"
    }

    @Test
    fun `cancelando las reservas afectadas en la misma transacción, el bloqueo se crea`() {
        val franja = franjaDePrueba("09:00", "13:00")
        reservas.create(reservaDePrueba(usuarios.conMail(JUAN), cancha3, franjaDePrueba("09:00", "10:30")))
        val ahora = LocalDateTime.now()

        val afectadas = reservas.findConfirmadasSuperpuestas(cancha3.id, franja, ahora)
        afectadas.forEach {
            it.cancelarPorMantenimiento(ahora)
            reservas.update(it)
        }
        val bloqueo = bloqueos.create(bloqueoDePrueba(cancha3, franja, daniela, "Arreglo de red"))

        afectadas shouldHaveSize 1
        bloqueos.findById(bloqueo.id)!!.motivo shouldBe "Arreglo de red"
    }

    @Test
    fun `la base no deja que cargue un bloqueo alguien que no es admin`() {
        val error = shouldThrow<BusinessException> {
            bloqueos.create(bloqueoDePrueba(cancha3, franjaDePrueba("09:00", "13:00"), usuarios.conMail(JUAN)))
        }
        error.message shouldBe "Solo un administrador puede bloquear espacios"
    }

    @Test
    fun `modifica y borra un bloqueo`() {
        val bloqueo = bloqueos.create(bloqueoDePrueba(cancha3, franjaDePrueba("09:00", "13:00"), daniela))

        bloqueo.actualizar(cancha3, franjaDePrueba("14:00", "18:00"), "Pintura")
        bloqueos.update(bloqueo)
        bloqueos.findById(bloqueo.id)!!.franja shouldBe franjaDePrueba("14:00", "18:00")

        bloqueos.delete(bloqueo.id).shouldBeTrue()
        bloqueos.findById(bloqueo.id).shouldBeNull()
    }

    @Test
    fun `lee los espacios activos con su tipo, y un usuario que no existe no aparece`() {
        val todos = espacios.findAll()

        todos shouldHaveSize 6
        espacios.llamado("Parrilla 1").tipo.duracionMin shouldBe 240
        usuarios.findById(9999).shouldBeNull()
    }
}
