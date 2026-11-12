package ar.edu.unsam.reservas.repository.mongo

import ar.edu.unsam.reservas.domain.EventoHistorial
import ar.edu.unsam.reservas.domain.TipoEvento
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.time.LocalDateTime

// Corre contra el MongoDB de docker-compose. Mongo no tiene el rollback de los
// tests de PostgreSQL: los eventos de prueba usan un usuario que no existe y se
// borran al terminar cada test.
@SpringBootTest
@DisplayName("Dado el historial en MongoDB")
class HistorialRepositoryMongoTest(
    @Autowired val historial: HistorialRepositoryMongo,
    @Autowired val mongo: MongoTemplate,
) {
    private val usuarioDePrueba = 9999L

    @AfterEach
    fun borrarEventosDePrueba() {
        mongo.remove(Query(Criteria.where("usuarioId").`is`(usuarioDePrueba)), HistorialRepositoryMongo.COLECCION)
    }

    @Test
    fun `guarda un evento con su detalle y lo lee igual`() {
        val evento = EventoHistorial(
            usuarioDePrueba,
            TipoEvento.RESERVA_RECHAZADA,
            LocalDateTime.of(2026, 11, 12, 10, 0),
            mapOf(
                "espacio" to "Cancha 1",
                "inicio" to LocalDateTime.of(2026, 11, 13, 18, 0),
                "fin" to LocalDateTime.of(2026, 11, 13, 19, 0),
                "motivo" to "El turno ya está reservado",
            ),
        )

        historial.registrar(evento)
        val leidos = historial.findByUsuario(usuarioDePrueba)

        evento.id shouldNotBe null
        leidos shouldHaveSize 1
        leidos.first().id shouldBe evento.id
        leidos.first().tipo shouldBe TipoEvento.RESERVA_RECHAZADA
        leidos.first().fecha shouldBe LocalDateTime.of(2026, 11, 12, 10, 0)
        leidos.first().detalle shouldBe evento.detalle
    }

    @Test
    fun `cada tipo de evento guarda solo sus datos`() {
        historial.registrar(
            EventoHistorial(usuarioDePrueba, TipoEvento.RESERVA_CANCELADA, LocalDateTime.of(2026, 11, 12, 8, 30),
                mapOf("reservaId" to 8L, "espacio" to "Parrilla 2", "anticipacionHoras" to 49L)),
        )

        historial.findByUsuario(usuarioDePrueba).first().detalle.keys shouldBe
            setOf("reservaId", "espacio", "anticipacionHoras")
    }

    @Test
    fun `devuelve el historial de un usuario con lo último primero`() {
        val horas = listOf(9, 15, 12)
        horas.forEach {
            historial.registrar(
                EventoHistorial(usuarioDePrueba, TipoEvento.RESERVA_CREADA, LocalDateTime.of(2026, 11, 12, it, 0), mapOf()),
            )
        }

        historial.findByUsuario(usuarioDePrueba).map { it.fecha.hour } shouldBe listOf(15, 12, 9)
    }

    @Test
    fun `lee los eventos que carga el script de datos`() {
        val deSofia = historial.findByUsuario(4)

        deSofia.map { it.tipo } shouldBe listOf(TipoEvento.RESERVA_CANCELADA, TipoEvento.RESERVA_CREADA)
        deSofia.first().detalle["anticipacionHoras"] shouldBe 49L
    }
}
