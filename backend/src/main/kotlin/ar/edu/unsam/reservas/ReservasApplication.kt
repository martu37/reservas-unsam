package ar.edu.unsam.reservas

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.TimeZone

@SpringBootApplication
class ReservasApplication

// La misma zona horaria que la base (docker-compose.yml). En Windows Java puede
// informar el nombre viejo "America/Buenos_Aires", que PostgreSQL ya no acepta.
const val ZONA_HORARIA = "America/Argentina/Buenos_Aires"

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone(ZONA_HORARIA))
    runApplication<ReservasApplication>(*args)
}
