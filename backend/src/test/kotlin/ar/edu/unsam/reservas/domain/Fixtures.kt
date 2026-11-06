package ar.edu.unsam.reservas.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

val MARTES: LocalDate = LocalDate.of(2026, 11, 10)

fun hora(texto: String, dia: LocalDate = MARTES): LocalDateTime = dia.atTime(LocalTime.parse(texto))

fun franja(desde: String, hasta: String, dia: LocalDate = MARTES) = Franja(hora(desde, dia), hora(hasta, dia))

fun parrilla() = TipoEspacio().apply {
    id = 3
    nombre = "Parrilla"
    duracionMin = 240
    horaApertura = LocalTime.of(10, 0)
    horaCierre = LocalTime.of(20, 0)
}

fun futbol5() = TipoEspacio().apply {
    id = 1
    nombre = "Cancha fútbol 5"
    duracionMin = 60
    horaApertura = LocalTime.of(9, 0)
    horaCierre = LocalTime.of(22, 0)
}

fun futbol7() = TipoEspacio().apply {
    id = 2
    nombre = "Cancha fútbol 7"
    duracionMin = 90
    horaApertura = LocalTime.of(9, 0)
    horaCierre = LocalTime.of(22, 0)
}

fun cancha1() = Espacio().apply {
    id = 1
    nombre = "Cancha 1"
    tipo = futbol5()
    ubicacion = "Sector deportivo"
}

fun juan() = Usuario().apply {
    id = 1
    mail = "juan.perez@estudiantes.unsam.edu.ar"
    apellido = "Pérez"
    nombre = "Juan"
}

fun daniela() = Usuario().apply {
    id = 5
    mail = "dfernandez@unsam.edu.ar"
    apellido = "Fernández"
    nombre = "Daniela"
    claustro = Claustro.NO_DOCENTE
    rol = Rol.ADMIN
}
