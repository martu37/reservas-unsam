package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.Usuario
import ar.edu.unsam.reservas.repository.UsuarioRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UsuarioRepositoryPostgres(private val jdbc: NamedParameterJdbcTemplate) : UsuarioRepository {

    private val select = "SELECT ${columnasUsuario("u")} FROM usuario u"

    override fun findAll(): List<Usuario> =
        jdbc.query("$select ORDER BY u.id", emptyMap<String, Any>()) { rs, _ -> rs.usuario("u") }

    override fun findById(id: Long): Usuario? =
        jdbc.query("$select WHERE u.id = :id", mapOf("id" to id)) { rs, _ -> rs.usuario("u") }
            .firstOrNull()
}
