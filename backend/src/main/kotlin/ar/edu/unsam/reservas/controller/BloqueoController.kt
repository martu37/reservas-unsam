package ar.edu.unsam.reservas.controller

import ar.edu.unsam.reservas.dto.BloqueoDTO
import ar.edu.unsam.reservas.dto.BloqueoDataDTO
import ar.edu.unsam.reservas.dto.toDTO
import ar.edu.unsam.reservas.service.BloqueoService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

// El CRUD de bloqueos. Las altas, cambios y bajas las hace un admin:
// el service controla que ?usuarioId= sea administrador.
@RestController
@CrossOrigin(origins = ["http://localhost:5173"])
class BloqueoController(private val bloqueoService: BloqueoService) {

    @GetMapping("/bloqueos")
    fun getBloqueos(): List<BloqueoDTO> = bloqueoService.todos().map { it.toDTO() }

    @GetMapping("/bloqueos/{id}")
    fun getBloqueo(@PathVariable id: Long): BloqueoDTO = bloqueoService.porId(id).toDTO()

    @PostMapping("/bloqueos")
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(@RequestParam usuarioId: Long, @RequestBody data: BloqueoDataDTO): BloqueoDTO =
        bloqueoService.crear(usuarioId, data.espacioId, data.franja(), data.motivo).toDTO()

    @PutMapping("/bloqueos/{id}")
    fun actualizar(
        @PathVariable id: Long,
        @RequestParam usuarioId: Long,
        @RequestBody data: BloqueoDataDTO,
    ): BloqueoDTO =
        bloqueoService.actualizar(id, usuarioId, data.espacioId, data.franja(), data.motivo).toDTO()

    @DeleteMapping("/bloqueos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminar(@PathVariable id: Long, @RequestParam usuarioId: Long) =
        bloqueoService.eliminar(id, usuarioId)
}
