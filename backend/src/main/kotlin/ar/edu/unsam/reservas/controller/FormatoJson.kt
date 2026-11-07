package ar.edu.unsam.reservas.controller

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.format.DateTimeFormatter

// Cómo salen las fechas en el JSON. El front las compara como texto
// ('2026-11-20T18:00:00' <= ahora), así que tienen que tener siempre el mismo
// formato: sin esto, Jackson manda '2026-11-20T18:00' cuando los segundos son 0.
@Configuration
class FormatoJson {
    @Bean
    fun formatoDeFechas() = Jackson2ObjectMapperBuilderCustomizer {
        it.serializers(
            LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
            LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm")),
        )
    }
}
