package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.BusinessException
import ar.edu.unsam.reservas.domain.TurnoNoDisponibleException
import org.postgresql.util.PSQLException
import org.springframework.dao.DataAccessException

// Cuando la base rechaza algo (UNIQUE, CHECK o FK), Spring lanza una
// DataAccessException. Acá se traduce a las excepciones del dominio, así el
// resto del backend no conoce los errores de PostgreSQL.

// El índice único de reserva: alguien reservó ese turno antes
private const val TURNO_TOMADO = "uq_reserva_turno"

private val MENSAJES = mapOf(
    TURNO_TOMADO to "El turno ya está reservado",
    "uq_usuario_mail" to "Ya hay un usuario con ese mail",
    "ck_usuario_mail" to "Hay que usar el mail institucional de la universidad",
    "ck_bloqueo_motivo" to "El bloqueo necesita un motivo",
    "fk_reserva_espacio" to "El espacio tiene reservas y no se puede borrar",
    "fk_reserva_usuario" to "El usuario no existe o tiene reservas",
    "fk_bloqueo_creado_por" to "El usuario no existe o cargó bloqueos",
)

internal fun <T> traducirErrores(accion: () -> T): T =
    try {
        accion()
    } catch (error: DataAccessException) {
        throw traducir(error)
    }

private fun traducir(error: DataAccessException): RuntimeException {
    val psql = generateSequence<Throwable>(error) { it.cause }
        .filterIsInstance<PSQLException>()
        .firstOrNull() ?: return error
    val restriccion = psql.serverErrorMessage?.constraint
    val mensaje = MENSAJES[restriccion] ?: psql.serverErrorMessage?.message ?: "Error en la base de datos"

    return if (restriccion == TURNO_TOMADO) {
        TurnoNoDisponibleException(mensaje)
    } else {
        BusinessException(mensaje)
    }
}
