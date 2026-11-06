package ar.edu.unsam.reservas.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class FranjaSpec : DescribeSpec({
    describe("una franja horaria") {
        it("se pisa con otra que empieza antes de que termine") {
            franja("18:00", "19:00").sePisaCon(franja("18:30", "19:30")).shouldBeTrue()
        }

        it("se pisa con otra que la contiene") {
            franja("18:00", "19:00").sePisaCon(franja("17:00", "20:00")).shouldBeTrue()
        }

        it("no se pisa con la siguiente aunque sean contiguas") {
            franja("18:00", "19:00").sePisaCon(franja("19:00", "20:00")).shouldBeFalse()
        }

        it("no puede terminar antes de empezar") {
            shouldThrow<BusinessException> { franja("19:00", "18:00") }
        }
    }
})
