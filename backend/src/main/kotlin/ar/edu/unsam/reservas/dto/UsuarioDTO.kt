package ar.edu.unsam.reservas.dto

import ar.edu.unsam.reservas.domain.Usuario

// Lo que el front recibe de un usuario: con la forma de UsuarioJSON del front
data class UsuarioDTO(
    val id: Long,
    val mail: String,
    val apellido: String,
    val nombre: String,
    val claustro: String,
    val rol: String,
)

fun Usuario.toDTO() = UsuarioDTO(
    id = id,
    mail = mail,
    apellido = apellido,
    nombre = nombre,
    claustro = claustro.paraElFront().replace('_', ' '),
    rol = rol.paraElFront(),
)

// En la base y en Kotlin los valores van en mayúsculas (CONFIRMADA, NO_DOCENTE);
// el front los usa en minúsculas ('confirmada', 'no docente')
fun Enum<*>.paraElFront() = name.lowercase()
