package ar.edu.unsam.reservas.controller

import ar.edu.unsam.reservas.repository.EspacioRepository
import ar.edu.unsam.reservas.repository.UsuarioRepository
import ar.edu.unsam.reservas.repository.postgres.DANIELA
import ar.edu.unsam.reservas.repository.postgres.JUAN
import ar.edu.unsam.reservas.repository.postgres.conMail
import ar.edu.unsam.reservas.repository.postgres.llamado
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Dado el controller de bloqueos")
class BloqueoControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val usuarios: UsuarioRepository,
    @Autowired val espacios: EspacioRepository,
) {
    private val juan get() = usuarios.conMail(JUAN).id
    private val daniela get() = usuarios.conMail(DANIELA).id
    private val cancha1 get() = espacios.llamado("Cancha 1").id

    private fun bloquear(usuarioId: Long, desde: String, hasta: String, motivo: String = "Arco roto"): ResultActions =
        mockMvc.perform(
            post("/bloqueos")
                .param("usuarioId", usuarioId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(bloqueoJson(cancha1, desde, hasta, motivo)),
        )

    @Test
    fun `un admin crea el bloqueo y devuelve 201`() {
        bloquear(daniela, "10:00", "13:00")
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.espacioNombre").value("Cancha 1"))
            .andExpect(jsonPath("$.motivo").value("Arco roto"))
            .andExpect(jsonPath("$.creadoPor").value(daniela))
    }

    @Test
    fun `alguien que no es admin no puede bloquear`() {
        bloquear(juan, "10:00", "13:00")
            .andExpect(status().isBadRequest)
            .andExpect(mensaje("Solo un administrador puede gestionar bloqueos"))
    }

    @Test
    fun `bloquear cancela la reserva que caía adentro`() {
        val reserva = mockMvc.reservar(juan, cancha1, "11:00", "12:00").id()

        bloquear(daniela, "10:00", "13:00").andExpect(status().isCreated)

        mockMvc.perform(get("/usuarios/$juan/reservas"))
            .andExpect(jsonPath("$[?(@.id == $reserva)].estado").value("cancelada"))
    }

    @Test
    fun `al actualizar devuelve el bloqueo modificado`() {
        val id = bloquear(daniela, "10:00", "13:00").id()

        mockMvc.perform(
            put("/bloqueos/$id")
                .param("usuarioId", daniela.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(bloqueoJson(cancha1, "10:00", "14:00", "Arco y red rotos")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.fin").value(hora("14:00")))
            .andExpect(jsonPath("$.motivo").value("Arco y red rotos"))
    }

    @Test
    fun `al eliminar un bloqueo ya no se encuentra`() {
        val id = bloquear(daniela, "10:00", "13:00").id()

        mockMvc.perform(delete("/bloqueos/$id").param("usuarioId", daniela.toString()))
            .andExpect(status().isNoContent)
        mockMvc.perform(get("/bloqueos/$id"))
            .andExpect(status().isNotFound)
    }
}
