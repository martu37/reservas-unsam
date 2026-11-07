package ar.edu.unsam.reservas.controller

import ar.edu.unsam.reservas.dto.NuevaReservaDTO
import ar.edu.unsam.reservas.dto.ReservaDTO
import ar.edu.unsam.reservas.dto.toDTO
import ar.edu.unsam.reservas.service.ReservaService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

// Quién hace el pedido llega como ?usuarioId=. No hay login: en la demo se elige
// el usuario en el front ("Usando como"). Con login saldría de la sesión.
@RestController
@CrossOrigin(origins = ["http://localhost:5173"])
class ReservaController(private val reservaService: ReservaService) {

    @GetMapping("/usuarios/{usuarioId}/reservas")
    fun getReservasDeUsuario(@PathVariable usuarioId: Long): List<ReservaDTO> =
        reservaService.deUsuario(usuarioId).map { it.toDTO() }

    // 201 si se confirmó; 409 si el turno está ocupado o bloqueado; 400 si no es un turno válido
    @PostMapping("/reservas")
    @ResponseStatus(HttpStatus.CREATED)
    fun reservar(@RequestParam usuarioId: Long, @RequestBody pedido: NuevaReservaDTO): ReservaDTO =
        reservaService.reservar(usuarioId, pedido.espacioId, pedido.franja()).toDTO()

    // No se borra: queda cancelada, con la fecha en que se canceló
    @PatchMapping("/reservas/{id}/cancelar")
    fun cancelar(@PathVariable id: Long, @RequestParam usuarioId: Long): ReservaDTO =
        reservaService.cancelar(id, usuarioId).toDTO()
}
