package ar.edu.unsam.reservas.controller

import ar.edu.unsam.reservas.repository.postgres.DANIELA
import ar.edu.unsam.reservas.repository.postgres.JUAN
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Dado el ingreso con el mail institucional")
class UsuarioControllerTest(@Autowired val mockMvc: MockMvc) {

    private fun darDeAlta(mail: String): ResultActions =
        mockMvc.perform(
            post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"mail":"$mail","nombre":"Ana","apellido":"Ruiz","claustro":"no docente"}"""),
        )

    @Test
    fun `se encuentra a un usuario por su mail, sin importar las mayúsculas`() {
        mockMvc.perform(get("/usuarios/buscar").param("mail", "  DFernandez@unsam.edu.ar "))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mail").value(DANIELA))
            .andExpect(jsonPath("$.rol").value("admin"))
    }

    @Test
    fun `un mail que todavía no se dio de alta devuelve not found`() {
        mockMvc.perform(get("/usuarios/buscar").param("mail", "ana.ruiz@unsam.edu.ar"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `la primera vez se da de alta como usuario común y después ya se lo encuentra`() {
        darDeAlta("Ana.Ruiz@unsam.edu.ar")
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.mail").value("ana.ruiz@unsam.edu.ar"))
            .andExpect(jsonPath("$.claustro").value("no docente"))
            .andExpect(jsonPath("$.rol").value("usuario"))

        mockMvc.perform(get("/usuarios/buscar").param("mail", "ana.ruiz@unsam.edu.ar"))
            .andExpect(status().isOk)
    }

    @Test
    fun `no se puede dar de alta un mail que no es de la universidad`() {
        darDeAlta("ana.ruiz@gmail.com")
            .andExpect(status().isBadRequest)
            .andExpect(mensaje("Hay que usar el mail institucional de la universidad"))
    }

    @Test
    fun `no se puede dar de alta dos veces el mismo mail`() {
        darDeAlta(JUAN)
            .andExpect(status().isBadRequest)
            .andExpect(mensaje("Ya hay un usuario con ese mail"))
    }
}
