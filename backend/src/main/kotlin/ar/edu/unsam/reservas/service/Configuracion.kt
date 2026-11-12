package ar.edu.unsam.reservas.service

import ar.edu.unsam.reservas.repository.HistorialRepository
import ar.edu.unsam.reservas.repository.mongo.HistorialRepositoryMongo
import ar.edu.unsam.reservas.repository.postgres.HistorialRepositoryPostgres
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Clock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Configuration
class Configuracion {
    // El reloj del sistema como dependencia: los services no llaman a now() directo
    @Bean
    fun reloj(): Clock = Clock.systemDefaultZone()

    // Dónde se guarda el historial: lo dice historial.motor en application.yml
    // (o la variable de entorno HISTORIAL_MOTOR). Los services solo ven la interfaz.
    @Bean
    @Primary
    fun historial(
        @Value("\${historial.motor}") motor: String,
        mongo: HistorialRepositoryMongo,
        postgres: HistorialRepositoryPostgres,
    ): HistorialRepository = if (motor == "postgres") postgres else mongo
}

// Sin segundos ni nanos: así se guarda y se compara en la base
fun Clock.ahora(): LocalDateTime = LocalDateTime.now(this).truncatedTo(ChronoUnit.MINUTES)
