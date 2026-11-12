package ar.edu.unsam.reservas.controller

import ar.edu.unsam.reservas.repository.EspacioRepository
import ar.edu.unsam.reservas.repository.UsuarioRepository
import ar.edu.unsam.reservas.repository.postgres.DANIELA
import ar.edu.unsam.reservas.repository.postgres.DIA_DE_PRUEBA
import ar.edu.unsam.reservas.repository.postgres.JUAN
import ar.edu.unsam.reservas.repository.postgres.conMail
import ar.edu.unsam.reservas.repository.postgres.llamado
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Dadas las consultas de usuarios, espacios e historial")
class ConsultasControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val usuarios: UsuarioRepository,
    @Autowired val espacios: EspacioRepository,
) {
    private val juan get() = usuarios.conMail(JUAN).id
    private val daniela get() = usuarios.conMail(DANIELA).id
    private val cancha1 get() = espacios.llamado("Cancha 1").id

    @Test
    fun `el uso de los espacios trae una fila por espacio con su demanda`() {
        mockMvc.perform(get("/espacios/uso"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(6))
            .andExpect(jsonPath("$[0].espacio").exists())
            .andExpect(jsonPath("$[0].demanda").exists())
    }

    @Test
    fun `los usuarios llegan con claustro y rol como los usa el front`() {
        mockMvc.perform(get("/usuarios/$daniela"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mail").value(DANIELA))
            .andExpect(jsonPath("$.claustro").value("no docente"))
            .andExpect(jsonPath("$.rol").value("admin"))
    }

    @Test
    fun `un espacio llega con su tipo y el horario sin segundos`() {
        mockMvc.perform(get("/espacios/$cancha1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nombre").value("Cancha 1"))
            .andExpect(jsonPath("$.tipo.duracionMin").value(60))
            .andExpect(jsonPath("$.tipo.horaApertura").value("09:00"))
            .andExpect(jsonPath("$.tipo.horaCierre").value("22:00"))
    }

    @Test
    fun `un espacio que no existe devuelve not found`() {
        mockMvc.perform(get("/espacios/9999")).andExpect(status().isNotFound)
    }

    @Test
    fun `la grilla del día devuelve cada turno con su estado`() {
        mockMvc.reservar(juan, cancha1, "10:00", "11:00").andExpect(status().isCreated)

        mockMvc.perform(get("/espacios/$cancha1/turnos").param("fecha", DIA_DE_PRUEBA.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(13))
            .andExpect(jsonPath("$[0].inicio").value(hora("09:00")))
            .andExpect(jsonPath("$[0].estado").value("libre"))
            .andExpect(jsonPath("$[1].estado").value("reservado"))
    }

    @Test
    fun `la grilla sin fecha es un pedido mal hecho`() {
        mockMvc.perform(get("/espacios/$cancha1/turnos")).andExpect(status().isBadRequest)
    }

    @Test
    fun `el historial dice qué motor contestó y trae el último evento primero`() {
        mockMvc.reservar(juan, cancha1, "10:00", "11:00")
        mockMvc.reservar(juan, cancha1, "10:00", "11:00")

        mockMvc.perform(get("/usuarios/$juan/historial"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.motor").value("postgres"))
            .andExpect(jsonPath("$.eventos[0].tipo").value("reserva_rechazada"))
            .andExpect(jsonPath("$.eventos[0].detalle.motivo").value("El turno ya está reservado"))
            .andExpect(jsonPath("$.eventos[0].detalle.inicio").value(hora("10:00")))
            .andExpect(jsonPath("$.eventos[1].tipo").value("reserva_creada"))
    }
}
