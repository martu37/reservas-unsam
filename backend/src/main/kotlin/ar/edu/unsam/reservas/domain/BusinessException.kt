package ar.edu.unsam.reservas.domain

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

// Una regla de negocio que no se cumple: el controller la devuelve como 400
@ResponseStatus(HttpStatus.BAD_REQUEST)
open class BusinessException(mensaje: String) : RuntimeException(mensaje)

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(mensaje: String) : RuntimeException(mensaje)

// El turno está ocupado o bloqueado: 409, igual que cuando lo rechaza la base
@ResponseStatus(HttpStatus.CONFLICT)
class TurnoNoDisponibleException(mensaje: String) : BusinessException(mensaje)
