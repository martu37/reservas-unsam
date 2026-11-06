package ar.edu.unsam.reservas.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.LocalTime

class TipoEspacioSpec : DescribeSpec({
    describe("los turnos de un tipo de espacio") {
        it("salen del horario y la duración, sin pasarse del cierre") {
            parrilla().turnosDelDia(MARTES) shouldBe listOf(
                franja("10:00", "14:00"),
                franja("14:00", "18:00"),
            )
        }

        it("una cancha de 90 minutos arma 8 turnos entre las 9 y las 22") {
            val turnos = futbol7().turnosDelDia(MARTES)

            turnos shouldHaveSize 8
            turnos.last() shouldBe franja("19:30", "21:00")
        }

        it("un horario que no coincide con un turno no es válido") {
            parrilla().esTurnoValido(franja("12:00", "16:00")).shouldBeFalse()
            parrilla().esTurnoValido(franja("14:00", "18:00")).shouldBeTrue()
        }
    }

    describe("la validación de un tipo de espacio") {
        it("no acepta que cierre antes de abrir") {
            val tipo = parrilla().apply { horaCierre = LocalTime.of(9, 0) }

            shouldThrow<BusinessException> { tipo.validate() }
        }

        it("no acepta turnos de duración cero") {
            val tipo = parrilla().apply { duracionMin = 0 }

            shouldThrow<BusinessException> { tipo.validate() }
        }
    }
})
