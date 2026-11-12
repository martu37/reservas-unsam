# Backend y frontend

## Cómo está armado

```
frontend (React)          pantallas: disponibilidad, mis reservas, actividad, bloqueos
      |  HTTP / JSON
backend (Spring Boot)
   controller            recibe el pedido y devuelve JSON (DTO)
   service               coordina: reglas del dominio + transacción + historial
   domain                las reglas del negocio (Espacio.reservar, Reserva.cancelar...)
   repository            interfaces: qué se guarda y se busca, sin decir dónde
      |            \
 PostgreSQL       MongoDB
```

El frontend nunca habla con las bases: todo pasa por el backend.

## Backend polimórfico

Los services dependen de **interfaces** de repositorio
(`backend/src/main/kotlin/.../repository/Repositorios.kt`), no de una base en
particular. Cada interfaz tiene su implementación:

| repositorio | implementación |
|---|---|
| usuarios, espacios, reservas, bloqueos | PostgreSQL (`repository/postgres`) |
| historial | MongoDB (`repository/mongo`, la principal) y PostgreSQL (el respaldo) |

Para cambiar dónde se guarda el historial no se toca ningún service: lo elige
`historial.motor` en `application.yml` (`mongo` o `postgres`). En Docker se
cambia al levantar:

```
HISTORIAL_MOTOR=postgres make start
```

El bean que arma esa elección está en `service/Configuracion.kt`. En "Mi
actividad" el front muestra qué motor contestó.

Orden al reservar: primero se confirma en PostgreSQL y después se registra el
evento en el historial. Si el historial falla, la reserva ya quedó hecha.

## API

Con el backend levantado queda en `http://localhost:8080`.

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/usuarios`, `/usuarios/{id}` | usuarios |
| GET | `/usuarios/buscar?mail=` | ingreso: el usuario de ese mail (404 si no se dio de alta) |
| POST | `/usuarios` | alta (`mail`, `nombre`, `apellido`, `claustro`), siempre como usuario común |
| GET | `/espacios`, `/espacios/{id}` | espacios activos, con su tipo |
| GET | `/espacios/{id}/turnos?fecha=AAAA-MM-DD` | grilla del día con el estado de cada turno |
| GET | `/espacios/uso` | reservas confirmadas y canceladas de cada espacio (la consulta de `database/consultas.sql`) |
| GET | `/usuarios/{id}/reservas` | reservas de un usuario |
| POST | `/reservas?usuarioId=` | reservar (`espacioId`, `inicio`, `fin`) |
| PATCH | `/reservas/{id}/cancelar?usuarioId=` | cancelar una reserva propia |
| GET | `/usuarios/{id}/historial` | historial de eventos y qué motor contestó |
| GET | `/bloqueos`, `/bloqueos/{id}` | bloqueos por mantenimiento |
| POST, PUT, DELETE | `/bloqueos`, `/bloqueos/{id}` con `?usuarioId=` | alta, cambio y baja de bloqueos (solo admin) |

No hay contraseña: se entra con el mail institucional (el front controla que sea
de la UNSAM y la base lo vuelve a controlar con `ck_usuario_mail`). Después,
quién hace cada pedido va en `?usuarioId=`.

Errores: 400 si no se cumple una regla, 404 si no existe, 409 si el turno está
ocupado o bloqueado. El cuerpo trae el motivo en `message`.

## Tests

```
cd backend
./gradlew test
```

Prueban el dominio, los repositorios contra el PostgreSQL de Docker, los
services y los controllers. Los que usan la base corren en una transacción que
se deshace al terminar: no modifican los datos.

En el frontend: `cd frontend` y `pnpm test`.
