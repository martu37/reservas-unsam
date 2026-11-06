package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.Bloqueo
import ar.edu.unsam.reservas.domain.Franja
import ar.edu.unsam.reservas.repository.BloqueoRepository
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class BloqueoRepositoryPostgres(private val jdbc: NamedParameterJdbcTemplate) : BloqueoRepository {

    // a = el admin que cargó el bloqueo
    private val select = """
        SELECT b.id, b.inicio, b.fin, b.motivo, b.creado_en,
               ${columnasUsuario("a")},
               $COLUMNAS_ESPACIO
          FROM bloqueo b
          JOIN usuario a      ON a.id = b.creado_por
          JOIN espacio e      ON e.id = b.espacio_id
          JOIN tipo_espacio t ON t.id = e.tipo_espacio_id
    """

    private val mapper = RowMapper { rs, _ ->
        Bloqueo(
            espacio = rs.espacio(),
            franja = Franja(rs.fechaHora("inicio")!!, rs.fechaHora("fin")!!),
            motivo = rs.getString("motivo"),
            creadoPor = rs.usuario("a"),
            creadoEn = rs.fechaHora("creado_en")!!,
        ).also { it.id = rs.getLong("id") }
    }

    override fun findAll(): List<Bloqueo> =
        jdbc.query("$select ORDER BY b.inicio", emptyMap<String, Any>(), mapper)

    override fun findById(id: Long): Bloqueo? =
        jdbc.query("$select WHERE b.id = :id", mapOf("id" to id), mapper).firstOrNull()

    override fun findSuperpuestos(espacioId: Long, franja: Franja): List<Bloqueo> =
        jdbc.query(
            """
            $select
             WHERE b.espacio_id = :espacioId
               AND tsrange(b.inicio, b.fin) && tsrange(CAST(:inicio AS timestamp), CAST(:fin AS timestamp))
            """,
            mapOf("espacioId" to espacioId, "inicio" to franja.inicio, "fin" to franja.fin),
            mapper,
        )

    override fun create(bloqueo: Bloqueo): Bloqueo = traducirErrores {
        bloqueo.id = jdbc.queryForObject(
            """
            INSERT INTO bloqueo (espacio_id, inicio, fin, motivo, creado_por, creado_en)
            VALUES (:espacioId, :inicio, :fin, :motivo, :creadoPor, :creadoEn)
            RETURNING id
            """,
            parametros(bloqueo) + mapOf("creadoPor" to bloqueo.creadoPor.id, "creadoEn" to bloqueo.creadoEn),
            Long::class.java,
        )!!
        bloqueo
    }

    override fun update(bloqueo: Bloqueo) {
        traducirErrores {
            jdbc.update(
                """
                UPDATE bloqueo
                   SET espacio_id = :espacioId, inicio = :inicio, fin = :fin, motivo = :motivo
                 WHERE id = :id
                """,
                parametros(bloqueo) + ("id" to bloqueo.id),
            )
        }
    }

    override fun delete(id: Long): Boolean =
        jdbc.update("DELETE FROM bloqueo WHERE id = :id", mapOf("id" to id)) > 0

    private fun parametros(bloqueo: Bloqueo) = mapOf(
        "espacioId" to bloqueo.espacio.id,
        "inicio" to bloqueo.inicio,
        "fin" to bloqueo.fin,
        "motivo" to bloqueo.motivo,
    )
}
