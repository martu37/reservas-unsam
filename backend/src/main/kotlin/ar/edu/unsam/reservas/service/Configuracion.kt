package ar.edu.unsam.reservas.service

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Configuration
class Configuracion {
    // El reloj del sistema como dependencia: los services no llaman a now() directo
    @Bean
    fun reloj(): Clock = Clock.systemDefaultZone()
}

// Sin segundos ni nanos: así se guarda y se compara en la base
fun Clock.ahora(): LocalDateTime = LocalDateTime.now(this).truncatedTo(ChronoUnit.MINUTES)
