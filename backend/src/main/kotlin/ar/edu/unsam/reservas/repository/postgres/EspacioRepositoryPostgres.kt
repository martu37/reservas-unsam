package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.Espacio
import ar.edu.unsam.reservas.repository.EspacioRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class EspacioRepositoryPostgres(private val jdbc: NamedParameterJdbcTemplate) : EspacioRepository {

    // El espacio se arma junto con su tipo: duración y horario están en tipo_espacio
    private val select = """
        SELECT $COLUMNAS_ESPACIO
          FROM espacio e
          JOIN tipo_espacio t ON t.id = e.tipo_espacio_id
    """

    override fun findAll(): List<Espacio> =
        jdbc.query("$select WHERE e.activo ORDER BY e.id", emptyMap<String, Any>()) { rs, _ -> rs.espacio() }

    override fun findById(id: Long): Espacio? =
        jdbc.query("$select WHERE e.id = :id", mapOf("id" to id)) { rs, _ -> rs.espacio() }
            .firstOrNull()
}
