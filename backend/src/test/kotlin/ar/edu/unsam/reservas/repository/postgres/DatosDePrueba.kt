package ar.edu.unsam.reservas.repository.postgres

import ar.edu.unsam.reservas.domain.Bloqueo
import ar.edu.unsam.reservas.domain.Espacio
import ar.edu.unsam.reservas.domain.Franja
import ar.edu.unsam.reservas.domain.Reserva
import ar.edu.unsam.reservas.domain.Usuario
import ar.edu.unsam.reservas.repository.EspacioRepository
import ar.edu.unsam.reservas.repository.UsuarioRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Los tests crean sus propias reservas lejos en el futuro, para no depender
// del día en que se cargaron los datos iniciales
val DIA_DE_PRUEBA: LocalDate = LocalDate.now().plusDays(20)

fun franjaDePrueba(desde: String, hasta: String, dia: LocalDate = DIA_DE_PRUEBA) =
    Franja(dia.atTime(LocalTime.parse(desde)), dia.atTime(LocalTime.parse(hasta)))

fun UsuarioRepository.conMail(mail: String): Usuario = findAll().first { it.mail == mail }

fun EspacioRepository.llamado(nombre: String): Espacio = findAll().first { it.nombre == nombre }

fun reservaDePrueba(usuario: Usuario, espacio: Espacio, franja: Franja) =
    Reserva(usuario, espacio, franja, LocalDateTime.now().withNano(0))

fun bloqueoDePrueba(espacio: Espacio, franja: Franja, admin: Usuario, motivo: String = "Corte de pasto") =
    Bloqueo(espacio, franja, motivo, admin, LocalDateTime.now().withNano(0))

const val JUAN = "juan.perez@estudiantes.unsam.edu.ar"
const val LUCIA = "lucia.gomez@estudiantes.unsam.edu.ar"
const val DANIELA = "dfernandez@unsam.edu.ar"
