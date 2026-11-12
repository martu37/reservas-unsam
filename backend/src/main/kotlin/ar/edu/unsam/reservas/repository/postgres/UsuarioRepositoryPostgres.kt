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

    // El mail es UNIQUE, así que trae uno o ninguno
    override fun findByMail(mail: String): Usuario? =
        jdbc.query("$select WHERE u.mail = :mail", mapOf("mail" to mail)) { rs, _ -> rs.usuario("u") }
            .firstOrNull()

    // rol y activo no se mandan: los pone la base con su DEFAULT (USUARIO y TRUE).
    // Si el mail no es de la UNSAM o ya existe, lo rechazan ck_usuario_mail y uq_usuario_mail
    override fun create(usuario: Usuario): Usuario = traducirErrores {
        usuario.id = jdbc.queryForObject(
            """
            INSERT INTO usuario (mail, apellido, nombre, claustro)
            VALUES (:mail, :apellido, :nombre, :claustro)
            RETURNING id
            """,
            mapOf(
                "mail" to usuario.mail,
                "apellido" to usuario.apellido,
                "nombre" to usuario.nombre,
                "claustro" to usuario.claustro.name,
            ),
            Long::class.java,
        )!!
        usuario
    }
}
