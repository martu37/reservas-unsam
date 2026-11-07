# Reservas UNSAM

TP Integrador de Base de Datos (Tecnicatura en Programación Informática).

Sistema de reservas de las canchas de fútbol y las parrillas de la universidad.
Hoy las reservas se piden por mail y alguien responde a mano; la idea es que el
sistema confirme en el momento si el espacio está libre.

## Cómo se reparten los motores

- **PostgreSQL**: usuarios (mail institucional), espacios y reservas. Es la
  fuente de verdad: la base no permite dos reservas superpuestas en el mismo
  espacio.
- **MongoDB**: historial de eventos de cada usuario, reportes de incidencias de
  los espacios y, si llegamos, "falta uno" para sumar jugadores a un partido.

## Estructura

```
backend/         API (Kotlin + Spring Boot)
frontend/        interfaz (React)
database/        scripts SQL y scripts de Mongo
documentacion/   informe y DER
```

## Cómo levantarlo

### Bases de datos

Hace falta Docker Desktop abierto:

```
docker compose up -d
```

- PostgreSQL en `localhost:5432` (usuario, contraseña y base: `reservas`)
- MongoDB en `localhost:27017`

### Backend

Hace falta Java 21. Los tests de los repositorios usan el PostgreSQL de Docker,
así que primero hay que levantar las bases:

```
cd backend
./gradlew test
```

Cada test de base corre en una transacción que se deshace al terminar: no
modifica los datos.

Para levantar la API en `http://localhost:8080`:

```
cd backend
./gradlew bootRun
```

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/usuarios`, `/usuarios/{id}` | usuarios |
| GET | `/espacios`, `/espacios/{id}` | espacios activos, con su tipo |
| GET | `/espacios/{id}/turnos?fecha=AAAA-MM-DD` | grilla del día con el estado de cada turno |
| GET | `/usuarios/{id}/reservas` | reservas de un usuario |
| POST | `/reservas?usuarioId=` | reservar (`espacioId`, `inicio`, `fin`) |
| PATCH | `/reservas/{id}/cancelar?usuarioId=` | cancelar una reserva propia |
| GET | `/usuarios/{id}/historial` | historial de eventos y qué motor contestó |
| GET | `/bloqueos`, `/bloqueos/{id}` | bloqueos por mantenimiento |
| POST, PUT, DELETE | `/bloqueos`, `/bloqueos/{id}` con `?usuarioId=` | alta, cambio y baja de bloqueos (solo admin) |

Errores: 400 si no se cumple una regla, 404 si no existe, 409 si el turno
está ocupado o bloqueado. El cuerpo trae el motivo en `message`.

### Frontend

Hace falta Node 20 o superior y pnpm:

```
cd frontend
pnpm install
pnpm dev
```

Queda en `http://localhost:5173`. Por ahora funciona con datos de prueba
(`frontend/src/mocks`), sin backend. Desde "Usando como" se puede cambiar de
usuario; Daniela Fernández es admin y ve la sección de bloqueos.

Tests: `pnpm test`.
