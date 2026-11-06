package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.BusinessException
import ar.edu.unsam.reservas.domain.TurnoNoDisponibleException
import org.postgresql.util.PSQLException
import org.springframework.dao.DataAccessException

// Cuando la base rechaza algo (EXCLUDE, CHECK, FK o un RAISE de un trigger),
// Spring lanza una DataAccessException. Acá se traduce a las excepciones del
// dominio, así el resto del backend no conoce los errores de PostgreSQL.

private const val EXCLUSION = "23P01" // EXCLUDE, y los triggers de "turno no disponible"

private val MENSAJES = mapOf(
    "ex_reserva_superpuesta" to "El turno ya está reservado",
    "ex_bloqueo_superpuesto" to "Ya hay un bloqueo en ese horario",
    "uq_usuario_mail" to "Ya hay un usuario con ese mail",
    "ck_usuario_mail_institucional" to "Hay que usar el mail institucional de la universidad",
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
    val servidor = psql.serverErrorMessage
    val mensaje = MENSAJES[servidor?.constraint] ?: servidor?.message ?: "Error en la base de datos"

    // El resto (P0001 de un RAISE, 23505 UNIQUE, 23514 CHECK, 23503 FK) es una
    // regla de negocio que no se cumple
    return if (psql.sqlState == EXCLUSION) {
        TurnoNoDisponibleException(mensaje)
    } else {
        BusinessException(mensaje)
    }
}
