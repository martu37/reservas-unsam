package ar.edu.unsam.reservas.controller

import ar.edu.unsam.reservas.repository.postgres.DIA_DE_PRUEBA
import com.jayway.jsonpath.JsonPath
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import java.time.LocalDate

// Pedidos HTTP que repiten varios tests de controllers

fun hora(hora: String, dia: LocalDate = DIA_DE_PRUEBA) = "${dia}T$hora:00"

fun MockMvc.reservar(usuarioId: Long, espacioId: Long, desde: String, hasta: String): ResultActions =
    perform(
        post("/reservas")
            .param("usuarioId", usuarioId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"espacioId":$espacioId,"inicio":"${hora(desde)}","fin":"${hora(hasta)}"}"""),
    )

fun MockMvc.cancelar(reservaId: Long, usuarioId: Long): ResultActions =
    perform(patch("/reservas/$reservaId/cancelar").param("usuarioId", usuarioId.toString()))

fun bloqueoJson(espacioId: Long, desde: String, hasta: String, motivo: String) =
    """{"espacioId":$espacioId,"inicio":"${hora(desde)}","fin":"${hora(hasta)}","motivo":"$motivo"}"""

// El id que vino en la respuesta, para usarlo en el pedido siguiente
fun ResultActions.id(): Long =
    JsonPath.read<Number>(andReturn().response.contentAsString, "$.id").toLong()

// Con MockMvc no se arma el JSON de error de Spring Boot: el mensaje se mira
// en la excepción que resolvió el controller
fun mensaje(esperado: String) = ResultMatcher {
    org.junit.jupiter.api.Assertions.assertEquals(esperado, it.resolvedException?.message)
}
