-- Modelo relacional: Reservas UNSAM (PostgreSQL)
-- Sale del DER (documentacion/DER.md) y está en 3FN.


-- Usuarios: se identifican con el mail de la universidad
CREATE TABLE usuario (
    id        SERIAL       PRIMARY KEY,
    mail      VARCHAR(120) NOT NULL,
    apellido  VARCHAR(60)  NOT NULL,
    nombre    VARCHAR(60)  NOT NULL,
    claustro  VARCHAR(12)  NOT NULL,
    rol       VARCHAR(8)   NOT NULL DEFAULT 'USUARIO',
    activo    BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_usuario_mail UNIQUE (mail),
    -- ...@unsam.edu.ar o ...@estudiantes.unsam.edu.ar
    CONSTRAINT ck_usuario_mail CHECK (mail LIKE '%@%unsam.edu.ar'),
    CONSTRAINT ck_usuario_claustro CHECK (claustro IN ('ESTUDIANTE', 'DOCENTE', 'NO_DOCENTE')),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('USUARIO', 'ADMIN'))
);


-- Tipos de espacio: la duración del turno y el horario dependen del tipo,
-- no de cada espacio (por eso es otra tabla, 3FN)
CREATE TABLE tipo_espacio (
    id             SERIAL      PRIMARY KEY,
    nombre         VARCHAR(40) NOT NULL,
    duracion_min   INTEGER     NOT NULL,
    hora_apertura  TIME        NOT NULL,
    hora_cierre    TIME        NOT NULL,

    CONSTRAINT uq_tipo_espacio_nombre UNIQUE (nombre),
    CONSTRAINT ck_tipo_espacio_duracion CHECK (duracion_min > 0),
    CONSTRAINT ck_tipo_espacio_horario CHECK (hora_cierre > hora_apertura)
);


CREATE TABLE espacio (
    id               SERIAL      PRIMARY KEY,
    nombre           VARCHAR(40) NOT NULL,
    tipo_espacio_id  INTEGER     NOT NULL,
    ubicacion        VARCHAR(80) NOT NULL,
    activo           BOOLEAN     NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_espacio_nombre UNIQUE (nombre),
    -- RESTRICT: no se puede borrar un tipo mientras tenga espacios
    CONSTRAINT fk_espacio_tipo FOREIGN KEY (tipo_espacio_id)
        REFERENCES tipo_espacio (id) ON DELETE RESTRICT
);


CREATE TABLE reserva (
    id            SERIAL      PRIMARY KEY,
    usuario_id    INTEGER     NOT NULL,
    espacio_id    INTEGER     NOT NULL,
    inicio        TIMESTAMP   NOT NULL,
    fin           TIMESTAMP   NOT NULL,
    estado        VARCHAR(10) NOT NULL DEFAULT 'CONFIRMADA',
    creada_en     TIMESTAMP   NOT NULL DEFAULT NOW(),
    cancelada_en  TIMESTAMP,

    -- RESTRICT: las reservas quedan como historia, no se borran en cascada
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT fk_reserva_espacio FOREIGN KEY (espacio_id)
        REFERENCES espacio (id) ON DELETE RESTRICT,
    CONSTRAINT ck_reserva_fin CHECK (fin > inicio),
    CONSTRAINT ck_reserva_estado CHECK (estado IN ('CONFIRMADA', 'CANCELADA')),
    -- Si está cancelada tiene fecha de cancelación; si no, no tiene
    CONSTRAINT ck_reserva_cancelada CHECK (
        (estado = 'CANCELADA' AND cancelada_en IS NOT NULL)
        OR (estado <> 'CANCELADA' AND cancelada_en IS NULL)
    )
);

-- La regla principal: un turno no se puede reservar dos veces.
-- Los turnos son fijos (salen de la duración y el horario del tipo), así que
-- dos reservas del mismo espacio se pisan solo si empiezan a la misma hora.
-- El WHERE hace que las canceladas no cuenten: ese turno se puede volver a
-- reservar. Como lo controla la base, no pasa aunque dos personas reserven
-- en el mismo momento.
CREATE UNIQUE INDEX uq_reserva_turno ON reserva (espacio_id, inicio)
    WHERE estado = 'CONFIRMADA';

-- "Mis reservas" busca por usuario y ordena por fecha
CREATE INDEX ix_reserva_usuario_inicio ON reserva (usuario_id, inicio);
-- La grilla del día busca por espacio y fecha
CREATE INDEX ix_reserva_espacio_inicio ON reserva (espacio_id, inicio);


-- Bloqueos por mantenimiento: mientras duran no se puede reservar el espacio
CREATE TABLE bloqueo (
    id          SERIAL       PRIMARY KEY,
    espacio_id  INTEGER      NOT NULL,
    inicio      TIMESTAMP    NOT NULL,
    fin         TIMESTAMP    NOT NULL,
    motivo      VARCHAR(120) NOT NULL,
    creado_por  INTEGER      NOT NULL,
    creado_en   TIMESTAMP    NOT NULL DEFAULT NOW(),

    -- CASCADE: si se borra un espacio, sus bloqueos ya no tienen sentido
    CONSTRAINT fk_bloqueo_espacio FOREIGN KEY (espacio_id)
        REFERENCES espacio (id) ON DELETE CASCADE,
    CONSTRAINT fk_bloqueo_creado_por FOREIGN KEY (creado_por)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT ck_bloqueo_fin CHECK (fin > inicio),
    CONSTRAINT ck_bloqueo_motivo CHECK (motivo <> '')
);


-- Historial de eventos: respaldo de MongoDB
-- El historial se guarda en MongoDB; esta tabla es la otra implementación del
-- mismo repositorio, por si se configura PostgreSQL o Mongo no responde.
-- Cada tipo de evento usa columnas distintas (un rechazo tiene motivo pero no
-- reserva), así que las que no corresponden quedan en NULL. En Mongo, en cambio,
-- cada documento guarda solo sus campos.
-- Es un registro de lo que pasó: guarda el nombre del espacio como era en ese
-- momento y no tiene FK, para no depender de que la reserva o el usuario sigan
-- existiendo.
CREATE TABLE historial_evento (
    id                  SERIAL       PRIMARY KEY,
    usuario_id          INTEGER      NOT NULL,
    tipo                VARCHAR(20)  NOT NULL,
    fecha               TIMESTAMP    NOT NULL,
    reserva_id          INTEGER,
    espacio             VARCHAR(40),
    inicio              TIMESTAMP,
    fin                 TIMESTAMP,
    motivo              VARCHAR(150),
    anticipacion_horas  INTEGER,

    CONSTRAINT ck_historial_tipo
        CHECK (tipo IN ('RESERVA_CREADA', 'RESERVA_CANCELADA', 'RESERVA_RECHAZADA'))
);

-- "Mi actividad" trae los eventos de un usuario, del más nuevo al más viejo
CREATE INDEX ix_historial_usuario_fecha ON historial_evento (usuario_id, fecha DESC);


-- Vista con los datos de cada reserva juntos (JOIN de 4 tablas)
CREATE VIEW reserva_detalle AS
SELECT r.id,
       r.inicio,
       r.fin,
       r.estado,
       r.creada_en,
       r.cancelada_en,
       u.id                          AS usuario_id,
       u.mail                        AS usuario_mail,
       u.nombre || ' ' || u.apellido AS usuario_nombre,
       e.id                          AS espacio_id,
       e.nombre                      AS espacio_nombre,
       e.ubicacion                   AS espacio_ubicacion,
       t.nombre                      AS tipo_espacio
  FROM reserva r
  JOIN usuario u      ON u.id = r.usuario_id
  JOIN espacio e      ON e.id = r.espacio_id
  JOIN tipo_espacio t ON t.id = e.tipo_espacio_id;
