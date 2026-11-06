package ar.edu.unsam.reservas.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EspacioSpec : DescribeSpec({
    val antes = hora("10:00")

    fun reservaDe(usuario: Usuario, franja: Franja) =
        cancha1().reservar(usuario, franja, emptyList(), emptyList(), antes)

    fun bloqueo(franja: Franja) =
        Bloqueo(cancha1(), franja, "Corte de pasto", daniela(), antes)

    describe("reservar un espacio") {
        it("un turno libre queda confirmado") {
            val reserva = reservaDe(juan(), franja("18:00", "19:00"))

            reserva.estado shouldBe EstadoReserva.CONFIRMADA
            reserva.espacio.nombre shouldBe "Cancha 1"
            reserva.creadaEn shouldBe antes
        }

        it("no se puede reservar un turno ocupado") {
            val existente = reservaDe(daniela(), franja("18:00", "19:00"))

            val error = shouldThrow<TurnoNoDisponibleException> {
                cancha1().reservar(juan(), franja("18:00", "19:00"), listOf(existente), emptyList(), antes)
            }
            error.message shouldBe "El turno ya está reservado"
        }

        it("un turno que se canceló se puede volver a reservar") {
            val cancelada = reservaDe(daniela(), franja("18:00", "19:00"))
                .apply { cancelar(daniela(), antes) }

            val reserva = cancha1().reservar(juan(), franja("18:00", "19:00"), listOf(cancelada), emptyList(), antes)

            reserva.estado shouldBe EstadoReserva.CONFIRMADA
        }

        it("no se puede reservar un espacio bloqueado y el error dice el motivo") {
            val error = shouldThrow<TurnoNoDisponibleException> {
                cancha1().reservar(juan(), franja("11:00", "12:00"), emptyList(), listOf(bloqueo(franja("09:00", "13:00"))), antes)
            }
            error.message shouldBe "El espacio está bloqueado: Corte de pasto"
        }

        it("no se puede reservar un turno que ya pasó") {
            shouldThrow<BusinessException> {
                cancha1().reservar(juan(), franja("09:00", "10:00"), emptyList(), emptyList(), antes)
            }
        }

        it("no se puede reservar un horario que no es un turno") {
            shouldThrow<BusinessException> { reservaDe(juan(), franja("18:30", "19:30")) }
        }

        it("no se puede reservar un espacio inactivo") {
            val espacio = cancha1().apply { activo = false }

            shouldThrow<BusinessException> {
                espacio.reservar(juan(), franja("18:00", "19:00"), emptyList(), emptyList(), antes)
            }
        }
    }

    describe("la grilla del día") {
        it("marca cada turno como libre, reservado, bloqueado o pasado") {
            val reservada = reservaDe(daniela(), franja("18:00", "19:00"))
            val bloqueada = bloqueo(franja("20:00", "22:00"))

            val estados = cancha1()
                .turnosPara(MARTES, listOf(reservada), listOf(bloqueada), hora("16:30"))
                .associate { it.franja.inicio.toLocalTime().toString() to it.estado }

            estados["16:00"] shouldBe EstadoTurno.PASADO
            estados["17:00"] shouldBe EstadoTurno.LIBRE
            estados["18:00"] shouldBe EstadoTurno.RESERVADO
            estados["20:00"] shouldBe EstadoTurno.BLOQUEADO
            estados["21:00"] shouldBe EstadoTurno.BLOQUEADO
        }
    }
})
