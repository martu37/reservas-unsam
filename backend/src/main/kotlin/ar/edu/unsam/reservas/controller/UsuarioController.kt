package ar.edu.unsam.reservas.controller

import ar.edu.unsam.reservas.dto.UsuarioDTO
import ar.edu.unsam.reservas.dto.toDTO
import ar.edu.unsam.reservas.service.UsuarioService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["http://localhost:5173"])
class UsuarioController(private val usuarioService: UsuarioService) {

    @GetMapping("/usuarios")
    fun getUsuarios(): List<UsuarioDTO> = usuarioService.todos().map { it.toDTO() }

    @GetMapping("/usuarios/{id}")
    fun getUsuario(@PathVariable id: Long): UsuarioDTO = usuarioService.porId(id).toDTO()
}
