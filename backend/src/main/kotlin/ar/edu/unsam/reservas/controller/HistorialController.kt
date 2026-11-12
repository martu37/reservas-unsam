package ar.edu.unsam.reservas.controller

import ar.edu.unsam.reservas.dto.HistorialDTO
import ar.edu.unsam.reservas.dto.toDTO
import ar.edu.unsam.reservas.service.HistorialService
import ar.edu.unsam.reservas.service.UsuarioService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["\${cors.origen}"])
class HistorialController(
    private val historialService: HistorialService,
    private val usuarioService: UsuarioService,
) {

    @GetMapping("/usuarios/{usuarioId}/historial")
    fun getHistorial(@PathVariable usuarioId: Long): HistorialDTO {
        usuarioService.porId(usuarioId)
        return HistorialDTO(
            motor = historialService.motor(),
            eventos = historialService.deUsuario(usuarioId).map { it.toDTO() },
        )
    }
}
