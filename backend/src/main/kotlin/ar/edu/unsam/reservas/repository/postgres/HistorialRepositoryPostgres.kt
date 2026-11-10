package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.EventoHistorial
import ar.edu.unsam.reservas.domain.TipoEvento
import ar.edu.unsam.reservas.repository.HistorialRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

// Respaldo del historial en PostgreSQL. Cada dato del detalle del evento va en
// su columna; los que ese tipo de evento no tiene quedan en NULL.
@Repository
class HistorialRepositoryPostgres(private val jdbc: NamedParameterJdbcTemplate) : HistorialRepository {

    override val motor = "postgres"

    override fun registrar(evento: EventoHistorial) {
        val detalle = evento.detalle
        val id = jdbc.queryForObject(
            """
            INSERT INTO historial_evento
                   (usuario_id, tipo, fecha, reserva_id, espacio, inicio, fin, motivo, anticipacion_horas)
            VALUES (:usuarioId, :tipo, :fecha, :reservaId, :espacio, :inicio, :fin, :motivo, :anticipacionHoras)
            RETURNING id
            """,
            mapOf(
                "usuarioId" to evento.usuarioId,
                "tipo" to evento.tipo.name,
                "fecha" to evento.fecha,
                "reservaId" to detalle["reservaId"],
                "espacio" to detalle["espacio"],
                "inicio" to detalle["inicio"],
                "fin" to detalle["fin"],
                "motivo" to detalle["motivo"],
                "anticipacionHoras" to detalle["anticipacionHoras"],
            ),
            Long::class.java,
        )
        evento.id = id.toString()
    }

    override fun findByUsuario(usuarioId: Long): List<EventoHistorial> =
        jdbc.query(
            """
            SELECT id, usuario_id, tipo, fecha, reserva_id, espacio, inicio, fin, motivo, anticipacion_horas
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
                detalle = rs.detalle(),
            ).also { it.id = rs.getLong("id").toString() }
        }

    // Arma el detalle solo con las columnas que tienen valor, como el documento de Mongo
    private fun ResultSet.detalle(): Map<String, Any?> = mapOf(
        "reservaId" to getObject("reserva_id")?.let { (it as Number).toLong() },
        "espacio" to getString("espacio"),
        "inicio" to fechaHora("inicio"),
        "fin" to fechaHora("fin"),
        "motivo" to getString("motivo"),
        "anticipacionHoras" to getObject("anticipacion_horas")?.let { (it as Number).toLong() },
    ).filterValues { it != null }
}
