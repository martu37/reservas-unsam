package ar.edu.unsam.reservas.controller

import ar.edu.unsam.reservas.dto.NuevoUsuarioDTO
import ar.edu.unsam.reservas.dto.UsuarioDTO
import ar.edu.unsam.reservas.dto.toDTO
import ar.edu.unsam.reservas.service.UsuarioService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["\${cors.origen}"])
class UsuarioController(private val usuarioService: UsuarioService) {

    @GetMapping("/usuarios")
    fun getUsuarios(): List<UsuarioDTO> = usuarioService.todos().map { it.toDTO() }

    // Ingreso: quién es el dueño de ese mail (404 si todavía no se dio de alta)
    @GetMapping("/usuarios/buscar")
    fun buscar(@RequestParam mail: String): UsuarioDTO = usuarioService.porMail(mail).toDTO()

    @GetMapping("/usuarios/{id}")
    fun getUsuario(@PathVariable id: Long): UsuarioDTO = usuarioService.porId(id).toDTO()

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(@RequestBody pedido: NuevoUsuarioDTO): UsuarioDTO =
        usuarioService.crear(pedido.mail, pedido.nombre, pedido.apellido, pedido.claustro()).toDTO()
}
