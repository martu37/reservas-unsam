package ar.edu.unsam.reservas.domain

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class UsuarioYBloqueoSpec : DescribeSpec({
    describe("el mail de un usuario") {
        it("puede ser de docente o de estudiante, siempre institucional") {
            shouldNotThrowAny { juan().validate() }
            shouldNotThrowAny { daniela().validate() }
        }

        it("no puede ser un mail personal") {
            val usuario = juan().apply { mail = "juanperez@gmail.com" }

            shouldThrow<BusinessException> { usuario.validate() }
        }
    }

    describe("las reservas de una persona") {
        val antes = hora("10:00")
        val aLas18 = cancha1().reservar(juan(), franja("18:00", "19:00"), emptyList(), emptyList(), antes)

        it("no puede tener dos reservas a la misma hora, aunque sean en espacios distintos") {
            val error = shouldThrow<TurnoNoDisponibleException> {
                juan().exigirLibreEn(franja("18:00", "19:00"), listOf(aLas18))
            }
            error.message shouldBe "Ya tenés una reserva en ese horario"
        }

        it("tampoco si los horarios se pisan aunque empiecen distinto") {
            shouldThrow<TurnoNoDisponibleException> {
                juan().exigirLibreEn(franja("17:30", "19:00"), listOf(aLas18))
            }
        }

        it("puede reservar el turno siguiente o si canceló la anterior") {
            val cancelada = cancha1().reservar(juan(), franja("20:00", "21:00"), emptyList(), emptyList(), antes)
                .apply { cancelar(juan(), antes) }

            shouldNotThrowAny { juan().exigirLibreEn(franja("19:00", "20:00"), listOf(aLas18)) }
            shouldNotThrowAny { juan().exigirLibreEn(franja("20:00", "21:00"), listOf(cancelada)) }
        }
    }

    describe("un bloqueo por mantenimiento") {
        it("solo lo puede cargar un administrador") {
            val bloqueo = Bloqueo(cancha1(), franja("09:00", "13:00"), "Corte de pasto", juan(), hora("08:00"))

            shouldThrow<BusinessException> { bloqueo.validate() }
        }

        it("necesita un motivo") {
            val bloqueo = Bloqueo(cancha1(), franja("09:00", "13:00"), " ", daniela(), hora("08:00"))

            shouldThrow<BusinessException> { bloqueo.validate() }
        }
    }
})
