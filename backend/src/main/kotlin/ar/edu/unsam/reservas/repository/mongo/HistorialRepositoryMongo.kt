package ar.edu.unsam.reservas.repository.mongo

import ar.edu.unsam.reservas.domain.EventoHistorial
import ar.edu.unsam.reservas.domain.TipoEvento
import ar.edu.unsam.reservas.repository.HistorialRepository
import org.bson.Document
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

// El historial en MongoDB, colección "historial". Cada evento es un documento y
// el detalle se guarda como viene: cada tipo de evento con sus propios campos.
@Repository
class HistorialRepositoryMongo(private val mongo: MongoTemplate) : HistorialRepository {

    override val motor = "mongo"

    override fun registrar(evento: EventoHistorial) {
        val documento = Document()
            .append("usuarioId", evento.usuarioId)
            .append("tipo", evento.tipo.name)
            .append("fecha", aFecha(evento.fecha))
            .append("detalle", Document(evento.detalle.mapValues { (_, valor) -> paraMongo(valor) }))
        val guardado = mongo.insert(documento, COLECCION)
        evento.id = guardado.getObjectId("_id").toHexString()
    }

    // Lo mismo que db.historial.find({ usuarioId: 1 }).sort({ fecha: -1 })
    override fun findByUsuario(usuarioId: Long): List<EventoHistorial> {
        val consulta = Query(Criteria.where("usuarioId").`is`(usuarioId))
            .with(Sort.by(Sort.Direction.DESC, "fecha"))
        return mongo.find(consulta, Document::class.java, COLECCION).map { it.aEvento() }
    }

    private fun Document.aEvento() = EventoHistorial(
        usuarioId = (get("usuarioId") as Number).toLong(),
        tipo = TipoEvento.valueOf(getString("tipo")),
        fecha = aFechaHora(getDate("fecha")),
        detalle = get("detalle", Document::class.java).mapValues { (_, valor) -> desdeMongo(valor) },
    ).also { it.id = getObjectId("_id").toHexString() }

    companion object {
        const val COLECCION = "historial"
    }
}

// MongoDB guarda las fechas en UTC. Se pasan con la zona horaria del servidor
// (Argentina) para que las 18:00 sigan siendo las 18:00 al leerlas.
private fun aFecha(fechaHora: LocalDateTime): Date = Date.from(fechaHora.atZone(ZoneId.systemDefault()).toInstant())

private fun aFechaHora(fecha: Date): LocalDateTime = LocalDateTime.ofInstant(fecha.toInstant(), ZoneId.systemDefault())

private fun paraMongo(valor: Any?): Any? = if (valor is LocalDateTime) aFecha(valor) else valor

// Los números del script de datos vienen como Integer: se leen todos como Long
private fun desdeMongo(valor: Any?): Any? = when (valor) {
    is Date -> aFechaHora(valor)
    is Number -> valor.toLong()
    else -> valor
}
