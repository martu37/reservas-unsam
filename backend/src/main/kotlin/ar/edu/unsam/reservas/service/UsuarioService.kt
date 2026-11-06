package ar.edu.unsam.reservas.service

import ar.edu.unsam.reservas.domain.NotFoundException
import ar.edu.unsam.reservas.domain.Usuario
import ar.edu.unsam.reservas.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class UsuarioService(private val usuarios: UsuarioRepository) {

    fun todos(): List<Usuario> = usuarios.findAll()

    fun porId(id: Long): Usuario =
        usuarios.findById(id) ?: throw NotFoundException("No se encontró el usuario $id")
}
