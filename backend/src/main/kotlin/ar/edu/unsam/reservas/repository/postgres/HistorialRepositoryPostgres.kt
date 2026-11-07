package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.EventoHistorial
import ar.edu.unsam.reservas.domain.TipoEvento
import ar.edu.unsam.reservas.repository.HistorialRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

// Respaldo del historial: el mismo documento que va a MongoDB, guardado como
// JSONB en una columna de PostgreSQL
@Repository
class HistorialRepositoryPostgres(
    private val jdbc: NamedParameterJdbcTemplate,
    private val json: ObjectMapper,
) : HistorialRepository {

    override val motor = "postgres"

    override fun registrar(evento: EventoHistorial) {
        val id = jdbc.queryForObject(
            """
            INSERT INTO historial_evento (usuario_id, tipo, fecha, detalle)
            VALUES (:usuarioId, :tipo, :fecha, CAST(:detalle AS jsonb))
            RETURNING id
            """,
            mapOf(
                "usuarioId" to evento.usuarioId,
                "tipo" to evento.tipo.name,
                "fecha" to evento.fecha,
                "detalle" to json.writeValueAsString(evento.detalle),
            ),
            Long::class.java,
        )
        evento.id = id.toString()
    }

    override fun findByUsuario(usuarioId: Long): List<EventoHistorial> =
        jdbc.query(
            """
            SELECT id, usuario_id, tipo, fecha, detalle::text AS detalle
              FROM historial_evento
             WHERE usuario_id = :usuarioId
             ORDER BY fecha DESC, id DESC
            """,
            mapOf("usuarioId" to usuarioId),
        ) { rs, _ ->
            EventoHistorial(
                usuarioId = rs.getLong("usuario_id"),
                tipo = TipoEvento.valueOf(rs.getString("tipo")),
                fecha = rs.fechaHora("fecha")!!,
                detalle = json.readValue<Map<String, Any?>>(rs.getString("detalle")),
            ).also { it.id = rs.getLong("id").toString() }
        }
}
