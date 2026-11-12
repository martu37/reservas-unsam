package ar.edu.unsam.reservas.service

import ar.edu.unsam.reservas.domain.Claustro
import ar.edu.unsam.reservas.domain.NotFoundException
import ar.edu.unsam.reservas.domain.Usuario
import ar.edu.unsam.reservas.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class UsuarioService(private val usuarios: UsuarioRepository) {

    fun todos(): List<Usuario> = usuarios.findAll()

    fun porId(id: Long): Usuario =
        usuarios.findById(id) ?: throw NotFoundException("No se encontró el usuario $id")

    // El ingreso: con el mail institucional se busca quién es. No hay contraseña
    // ni verificación del mail (es una prueba de concepto)
    fun porMail(mail: String): Usuario =
        usuarios.findByMail(normalizar(mail)) ?: throw NotFoundException("No hay un usuario con ese mail")

    // Alta desde la pantalla de ingreso: siempre como usuario común.
    // Los admins se cargan en la base (como Daniela en datos_iniciales.sql)
    fun crear(mail: String, nombre: String, apellido: String, claustro: Claustro): Usuario {
        val usuario = Usuario().apply {
            this.mail = normalizar(mail)
            this.nombre = nombre.trim()
            this.apellido = apellido.trim()
            this.claustro = claustro
        }
        usuario.validate()
        return usuarios.create(usuario)
    }

    // En la base los mails están en minúsculas: "Juan.Perez@..." es el mismo usuario
    private fun normalizar(mail: String) = mail.trim().lowercase()
}
