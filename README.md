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

Por ahora solo las bases (hace falta Docker Desktop abierto):

```
docker compose up -d
```

- PostgreSQL en `localhost:5432` (usuario, contraseña y base: `reservas`)
- MongoDB en `localhost:27017`
