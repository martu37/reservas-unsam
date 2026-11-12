package ar.edu.unsam.reservas.domain

enum class Claustro(val descripcion: String) {
    ESTUDIANTE("Estudiante"),
    DOCENTE("Docente"),
    NO_DOCENTE("No docente"),
}

enum class Rol(val descripcion: String) {
    USUARIO("Usuario"),
    ADMIN("Administrador"),
}

class Usuario {
    var id: Long = 0
    var mail: String = ""
    var apellido: String = ""
    var nombre: String = ""
    var claustro: Claustro = Claustro.ESTUDIANTE
    var rol: Rol = Rol.USUARIO
    var activo: Boolean = true

    val nombreCompleto get() = "$nombre $apellido"

    fun esAdmin() = rol == Rol.ADMIN

    // Una persona no puede estar en dos lugares a la vez: si ya tiene una
    // reserva que se pisa con ese horario (en cualquier espacio), no reserva otra.
    // La base lo controla también con uq_reserva_usuario_turno.
    fun exigirLibreEn(franja: Franja, susReservas: List<Reserva>) {
        if (susReservas.any { it.ocupa(franja) }) {
            throw TurnoNoDisponibleException("Ya tenés una reserva en ese horario")
        }
    }

    fun validate() {
        if (!MAIL_INSTITUCIONAL.matches(mail.trim().lowercase())) {
            throw BusinessException("Hay que usar el mail institucional de la universidad")
        }
        if (nombre.isBlank() || apellido.isBlank()) {
            throw BusinessException("El usuario necesita nombre y apellido")
        }
    }

    companion object {
        // usuario@unsam.edu.ar o usuario@estudiantes.unsam.edu.ar
        private val MAIL_INSTITUCIONAL = Regex("""^[^@\s]+@([a-z0-9-]+\.)?unsam\.edu\.ar$""")
    }
}
