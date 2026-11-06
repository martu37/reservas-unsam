package ar.edu.unsam.reservas.domain

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

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
