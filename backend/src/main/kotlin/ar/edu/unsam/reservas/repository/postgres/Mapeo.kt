package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.Claustro
import ar.edu.unsam.reservas.domain.Espacio
import ar.edu.unsam.reservas.domain.Rol
import ar.edu.unsam.reservas.domain.TipoEspacio
import ar.edu.unsam.reservas.domain.Usuario
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.LocalTime

// Un mismo SELECT trae columnas de varias tablas. Para que no choquen los
// nombres, cada tabla va con un prefijo: u_ (usuario), e_ (espacio), t_ (tipo).

internal fun columnasUsuario(alias: String) =
    listOf("id", "mail", "apellido", "nombre", "claustro", "rol", "activo")
        .joinToString(", ") { "$alias.$it AS ${alias}_$it" }

internal val COLUMNAS_ESPACIO = """
    e.id AS e_id, e.nombre AS e_nombre, e.ubicacion AS e_ubicacion, e.activo AS e_activo,
    t.id AS t_id, t.nombre AS t_nombre, t.duracion_min AS t_duracion_min,
    t.hora_apertura AS t_hora_apertura, t.hora_cierre AS t_hora_cierre
"""

internal fun ResultSet.usuario(alias: String) = Usuario().also {
    it.id = getLong("${alias}_id")
    it.mail = getString("${alias}_mail")
    it.apellido = getString("${alias}_apellido")
    it.nombre = getString("${alias}_nombre")
    it.claustro = Claustro.valueOf(getString("${alias}_claustro"))
    it.rol = Rol.valueOf(getString("${alias}_rol"))
    it.activo = getBoolean("${alias}_activo")
}

internal fun ResultSet.espacio() = Espacio().also {
    it.id = getLong("e_id")
    it.nombre = getString("e_nombre")
    it.ubicacion = getString("e_ubicacion")
    it.activo = getBoolean("e_activo")
    it.tipo = TipoEspacio().also { tipo ->
        tipo.id = getLong("t_id")
        tipo.nombre = getString("t_nombre")
        tipo.duracionMin = getInt("t_duracion_min")
        tipo.horaApertura = getObject("t_hora_apertura", LocalTime::class.java)
        tipo.horaCierre = getObject("t_hora_cierre", LocalTime::class.java)
    }
}

internal fun ResultSet.fechaHora(columna: String): LocalDateTime? =
    getObject(columna, LocalDateTime::class.java)
