package ar.edu.unsam.reservas.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe

class ReservaSpec : DescribeSpec({
    fun reserva() = Reserva(juan(), cancha1(), franja("18:00", "19:00"), hora("10:00"))

    describe("cancelar una reserva") {
        it("antes de que empiece la deja cancelada y libera el turno") {
            val reserva = reserva()

            reserva.cancelar(juan(), hora("17:00"))

            reserva.estado shouldBe EstadoReserva.CANCELADA
            reserva.canceladaEn shouldBe hora("17:00")
            reserva.ocupa(franja("18:00", "19:00")).shouldBeFalse()
        }

        it("no se puede una vez que empezó") {
            shouldThrow<BusinessException> { reserva().cancelar(juan(), hora("18:00")) }
        }

        it("no se puede cancelar la reserva de otra persona") {
            shouldThrow<NotFoundException> { reserva().cancelar(daniela(), hora("17:00")) }
        }

        it("no se puede cancelar dos veces") {
            val reserva = reserva().apply { cancelar(juan(), hora("17:00")) }

            shouldThrow<BusinessException> { reserva.cancelar(juan(), hora("17:10")) }
        }
    }

    describe("cancelar por mantenimiento") {
        it("la cancela aunque no lo pida su dueño") {
            val reserva = reserva()

            reserva.cancelarPorMantenimiento(hora("12:00"))

            reserva.estado shouldBe EstadoReserva.CANCELADA
        }

        it("no cancela una reserva que ya estaba cancelada") {
            val reserva = reserva().apply { cancelar(juan(), hora("12:00")) }

            shouldThrow<BusinessException> { reserva.cancelarPorMantenimiento(hora("13:00")) }
        }
    }

    describe("el evento de una cancelación") {
        it("guarda con cuántas horas de anticipación se canceló") {
            val reserva = reserva().apply { cancelar(juan(), hora("12:00")) }

            val evento = EventoHistorial.cancelada(reserva, hora("12:00"))

            evento.tipo shouldBe TipoEvento.RESERVA_CANCELADA
            evento.detalle["anticipacionHoras"] shouldBe 6L
            evento.detalle["espacio"] shouldBe "Cancha 1"
        }
    }
})
