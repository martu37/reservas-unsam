package ar.edu.unsam.reservas.controller

import ar.edu.unsam.reservas.repository.EspacioRepository
import ar.edu.unsam.reservas.repository.UsuarioRepository
import ar.edu.unsam.reservas.repository.postgres.JUAN
import ar.edu.unsam.reservas.repository.postgres.LUCIA
import ar.edu.unsam.reservas.repository.postgres.conMail
import ar.edu.unsam.reservas.repository.postgres.llamado
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Dado el controller de reservas")
class ReservaControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val usuarios: UsuarioRepository,
    @Autowired val espacios: EspacioRepository,
) {
    private val juan get() = usuarios.conMail(JUAN).id
    private val lucia get() = usuarios.conMail(LUCIA).id
    private val cancha1 get() = espacios.llamado("Cancha 1").id

    @Test
    fun `reservar un turno libre devuelve 201 con la reserva confirmada`() {
        mockMvc.reservar(juan, cancha1, "10:00", "11:00")
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.usuarioId").value(juan))
            .andExpect(jsonPath("$.espacioNombre").value("Cancha 1"))
            .andExpect(jsonPath("$.inicio").value(hora("10:00")))
            .andExpect(jsonPath("$.estado").value("confirmada"))
            .andExpect(jsonPath("$.canceladaEn").isEmpty)
    }

    @Test
    fun `reservar un turno ocupado devuelve 409 con el motivo`() {
        mockMvc.reservar(juan, cancha1, "10:00", "11:00")

        mockMvc.reservar(lucia, cancha1, "10:00", "11:00")
            .andExpect(status().isConflict)
            .andExpect(mensaje("El turno ya está reservado"))
    }

    @Test
    fun `un horario que no es un turno devuelve 400`() {
        mockMvc.reservar(juan, cancha1, "10:30", "11:30")
            .andExpect(status().isBadRequest)
            .andExpect(mensaje("El horario no corresponde a un turno"))
    }

    @Test
    fun `un fin anterior al inicio devuelve 400`() {
        mockMvc.reservar(juan, cancha1, "11:00", "10:00")
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `reservar sin decir quién reserva devuelve 400`() {
        mockMvc.perform(
            post("/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"espacioId":$cancha1,"inicio":"${hora("10:00")}","fin":"${hora("11:00")}"}"""),
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `la reserva nueva aparece en las reservas del usuario`() {
        val id = mockMvc.reservar(juan, cancha1, "10:00", "11:00").id()

        mockMvc.perform(get("/usuarios/$juan/reservas"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.id == $id)].estado").value("confirmada"))
    }

    @Test
    fun `cancelar la deja cancelada con la fecha de cancelación`() {
        val id = mockMvc.reservar(juan, cancha1, "10:00", "11:00").id()

        mockMvc.cancelar(id, juan)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("cancelada"))
            .andExpect(jsonPath("$.canceladaEn").isString)
    }

    @Test
    fun `cancelar la reserva de otra persona devuelve not found`() {
        val id = mockMvc.reservar(juan, cancha1, "10:00", "11:00").id()

        mockMvc.cancelar(id, lucia).andExpect(status().isNotFound)
    }
}
