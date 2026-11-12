package ar.edu.unsam.reservas.controller

import ar.edu.unsam.reservas.domain.UsoEspacio
import ar.edu.unsam.reservas.dto.EspacioDTO
import ar.edu.unsam.reservas.dto.TurnoDTO
import ar.edu.unsam.reservas.dto.toDTO
import ar.edu.unsam.reservas.service.EspacioService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@CrossOrigin(origins = ["\${cors.origen}"])
class EspacioController(private val espacioService: EspacioService) {

    @GetMapping("/espacios")
    fun getEspacios(): List<EspacioDTO> = espacioService.todos().map { it.toDTO() }

    // La consulta de agregación: reservas confirmadas y canceladas de cada espacio
    @GetMapping("/espacios/uso")
    fun getUso(): List<UsoEspacio> = espacioService.uso()

    @GetMapping("/espacios/{id}")
    fun getEspacio(@PathVariable id: Long): EspacioDTO = espacioService.porId(id).toDTO()

    // La grilla de un día: /espacios/2/turnos?fecha=2026-11-20
    @GetMapping("/espacios/{id}/turnos")
    fun getTurnos(@PathVariable id: Long, @RequestParam fecha: LocalDate): List<TurnoDTO> =
        espacioService.turnos(id, fecha).map { it.toDTO() }
}
