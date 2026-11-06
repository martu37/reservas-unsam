package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.EstadoReserva
import ar.edu.unsam.reservas.domain.Franja
import ar.edu.unsam.reservas.domain.Reserva
import ar.edu.unsam.reservas.repository.ReservaRepository
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
class ReservaRepositoryPostgres(private val jdbc: NamedParameterJdbcTemplate) : ReservaRepository {

    // La reserva completa: JOIN con usuario, espacio y tipo de espacio
    private val select = """
        SELECT r.id, r.inicio, r.fin, r.estado, r.creada_en, r.cancelada_en,
               ${columnasUsuario("u")},
               $COLUMNAS_ESPACIO
          FROM reserva r
          JOIN usuario u      ON u.id = r.usuario_id
          JOIN espacio e      ON e.id = r.espacio_id
          JOIN tipo_espacio t ON t.id = e.tipo_espacio_id
    """

    private val mapper = RowMapper { rs, _ ->
        Reserva(
            usuario = rs.usuario("u"),
            espacio = rs.espacio(),
            franja = Franja(rs.fechaHora("inicio")!!, rs.fechaHora("fin")!!),
            creadaEn = rs.fechaHora("creada_en")!!,
        ).also {
            it.id = rs.getLong("id")
            it.estado = EstadoReserva.valueOf(rs.getString("estado"))
            it.canceladaEn = rs.fechaHora("cancelada_en")
        }
    }

    override fun findById(id: Long): Reserva? =
        jdbc.query("$select WHERE r.id = :id", mapOf("id" to id), mapper).firstOrNull()

    override fun findByUsuario(usuarioId: Long): List<Reserva> =
        jdbc.query("$select WHERE r.usuario_id = :usuarioId ORDER BY r.inicio", mapOf("usuarioId" to usuarioId), mapper)

    // inicio >= día AND inicio < día siguiente (y no inicio::date = día) para que
    // la consulta pueda usar el índice ix_reserva_espacio_inicio
    override fun findByEspacioYDia(espacioId: Long, dia: LocalDate): List<Reserva> =
        jdbc.query(
            """
            $select
             WHERE r.espacio_id = :espacioId
               AND r.inicio >= :desde AND r.inicio < :hasta
             ORDER BY r.inicio
            """,
            mapOf("espacioId" to espacioId, "desde" to dia.atStartOfDay(), "hasta" to dia.plusDays(1).atStartOfDay()),
            mapper,
        )

    override fun findConfirmadasSuperpuestas(espacioId: Long, franja: Franja, desde: LocalDateTime): List<Reserva> =
        jdbc.query(
            """
            $select
             WHERE r.espacio_id = :espacioId
               AND r.estado = 'CONFIRMADA'
               AND r.inicio > :desde
               AND tsrange(r.inicio, r.fin) && tsrange(CAST(:inicio AS timestamp), CAST(:fin AS timestamp))
             ORDER BY r.inicio
            """,
            mapOf("espacioId" to espacioId, "desde" to desde, "inicio" to franja.inicio, "fin" to franja.fin),
            mapper,
        )

    override fun create(reserva: Reserva): Reserva = traducirErrores {
        reserva.id = jdbc.queryForObject(
            """
            INSERT INTO reserva (usuario_id, espacio_id, inicio, fin, estado, creada_en)
            VALUES (:usuarioId, :espacioId, :inicio, :fin, :estado, :creadaEn)
            RETURNING id
            """,
            mapOf(
                "usuarioId" to reserva.usuario.id,
                "espacioId" to reserva.espacio.id,
                "inicio" to reserva.inicio,
                "fin" to reserva.fin,
                "estado" to reserva.estado.name,
                "creadaEn" to reserva.creadaEn,
            ),
            Long::class.java,
        )!!
        reserva
    }

    // Lo único que cambia de una reserva es su estado (cancelarla)
    override fun update(reserva: Reserva) {
        traducirErrores {
            jdbc.update(
                "UPDATE reserva SET estado = :estado, cancelada_en = :canceladaEn WHERE id = :id",
                mapOf("id" to reserva.id, "estado" to reserva.estado.name, "canceladaEn" to reserva.canceladaEn),
            )
        }
    }
}
