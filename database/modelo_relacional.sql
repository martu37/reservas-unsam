-- Modelo relacional: Reservas UNSAM (PostgreSQL 16)
-- Sale del DER de documentacion/DER.md, en 3FN.

-- btree_gist permite combinar "=" sobre un número con "&&" sobre un rango
-- en la misma restricción de exclusión (ver reserva y bloqueo).
CREATE EXTENSION IF NOT EXISTS btree_gist;


-- ---------------------------------------------------------------------------
-- Usuarios: se identifican con el mail institucional
-- ---------------------------------------------------------------------------
CREATE TABLE usuario (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mail      VARCHAR(120) NOT NULL,
    apellido  VARCHAR(60)  NOT NULL,
    nombre    VARCHAR(60)  NOT NULL,
    claustro  VARCHAR(12)  NOT NULL,
    rol       VARCHAR(8)   NOT NULL DEFAULT 'USUARIO',
    activo    BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_usuario_mail UNIQUE (mail),
    CONSTRAINT ck_usuario_mail_institucional
        CHECK (mail = lower(mail) AND mail ~ '^[^@\s]+@([a-z0-9-]+\.)?unsam\.edu\.ar$'),
    CONSTRAINT ck_usuario_claustro CHECK (claustro IN ('ESTUDIANTE', 'DOCENTE', 'NO_DOCENTE')),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('USUARIO', 'ADMIN')),
    CONSTRAINT ck_usuario_nombre CHECK (btrim(nombre) <> '' AND btrim(apellido) <> '')
);


-- ---------------------------------------------------------------------------
-- Tipos de espacio: la duración del turno y el horario son del tipo (3FN)
-- ---------------------------------------------------------------------------
CREATE TABLE tipo_espacio (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre         VARCHAR(40) NOT NULL,
    duracion_min   INTEGER     NOT NULL,
    hora_apertura  TIME        NOT NULL,
    hora_cierre    TIME        NOT NULL,

    CONSTRAINT uq_tipo_espacio_nombre UNIQUE (nombre),
    CONSTRAINT ck_tipo_espacio_duracion CHECK (duracion_min > 0),
    CONSTRAINT ck_tipo_espacio_horario CHECK (hora_cierre > hora_apertura)
);


CREATE TABLE espacio (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre           VARCHAR(40) NOT NULL,
    tipo_espacio_id  BIGINT      NOT NULL,
    ubicacion        VARCHAR(80) NOT NULL,
    activo           BOOLEAN     NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_espacio_nombre UNIQUE (nombre),
    -- RESTRICT: no se puede borrar un tipo mientras tenga espacios
    CONSTRAINT fk_espacio_tipo FOREIGN KEY (tipo_espacio_id)
        REFERENCES tipo_espacio (id) ON DELETE RESTRICT
);


-- ---------------------------------------------------------------------------
-- Reservas
-- ---------------------------------------------------------------------------
CREATE TABLE reserva (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id    BIGINT      NOT NULL,
    espacio_id    BIGINT      NOT NULL,
    inicio        TIMESTAMP   NOT NULL,
    fin           TIMESTAMP   NOT NULL,
    estado        VARCHAR(10) NOT NULL DEFAULT 'CONFIRMADA',
    creada_en     TIMESTAMP   NOT NULL DEFAULT LOCALTIMESTAMP,
    cancelada_en  TIMESTAMP,

    -- RESTRICT: las reservas son historia, no se borran en cascada
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT fk_reserva_espacio FOREIGN KEY (espacio_id)
        REFERENCES espacio (id) ON DELETE RESTRICT,
    CONSTRAINT ck_reserva_fin CHECK (fin > inicio),
    CONSTRAINT ck_reserva_estado CHECK (estado IN ('CONFIRMADA', 'CANCELADA')),
    -- cancelada_en tiene valor si y solo si la reserva está cancelada
    CONSTRAINT ck_reserva_cancelada CHECK ((estado = 'CANCELADA') = (cancelada_en IS NOT NULL)),

    -- La regla central del sistema: dos reservas CONFIRMADAS del mismo espacio
    -- no pueden superponerse. tsrange(inicio, fin) es el rango [inicio, fin):
    -- un turno de 18 a 19 y otro de 19 a 20 no se pisan.
    -- La base la controla aunque dos personas reserven en el mismo instante.
    CONSTRAINT ex_reserva_superpuesta EXCLUDE USING gist (
        espacio_id WITH =,
        tsrange(inicio, fin) WITH &&
    ) WHERE (estado = 'CONFIRMADA')
);

-- "Mis reservas" busca por usuario ordenando por fecha
CREATE INDEX ix_reserva_usuario_inicio ON reserva (usuario_id, inicio);
-- La grilla del día busca por espacio y fecha
CREATE INDEX ix_reserva_espacio_inicio ON reserva (espacio_id, inicio);


-- ---------------------------------------------------------------------------
-- Bloqueos por mantenimiento
-- ---------------------------------------------------------------------------
CREATE TABLE bloqueo (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    espacio_id  BIGINT       NOT NULL,
    inicio      TIMESTAMP    NOT NULL,
    fin         TIMESTAMP    NOT NULL,
    motivo      VARCHAR(120) NOT NULL,
    creado_por  BIGINT       NOT NULL,
    creado_en   TIMESTAMP    NOT NULL DEFAULT LOCALTIMESTAMP,

    -- CASCADE: si se da de baja un espacio, sus bloqueos no tienen sentido
    CONSTRAINT fk_bloqueo_espacio FOREIGN KEY (espacio_id)
        REFERENCES espacio (id) ON DELETE CASCADE,
    CONSTRAINT fk_bloqueo_creado_por FOREIGN KEY (creado_por)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT ck_bloqueo_fin CHECK (fin > inicio),
    CONSTRAINT ck_bloqueo_motivo CHECK (btrim(motivo) <> ''),
    -- Dos bloqueos del mismo espacio tampoco se superponen: se edita el que ya existe
    CONSTRAINT ex_bloqueo_superpuesto EXCLUDE USING gist (
        espacio_id WITH =,
        tsrange(inicio, fin) WITH &&
    )
);


-- ---------------------------------------------------------------------------
-- Reglas entre dos tablas
-- ---------------------------------------------------------------------------
-- Un CHECK solo puede mirar la fila que se está guardando, y un EXCLUDE solo
-- compara filas de la misma tabla. Las reglas que cruzan reserva con bloqueo,
-- espacio o usuario van en triggers.
--
-- Los dos triggers bloquean la fila del espacio (SELECT ... FOR UPDATE).
-- Así, las reservas y los bloqueos de un mismo espacio se procesan de a uno:
-- si alguien reserva justo mientras un admin bloquea, uno de los dos espera
-- a que termine el otro y después ve sus datos.

CREATE FUNCTION validar_reserva() RETURNS trigger AS $$
DECLARE
    v_activo         BOOLEAN;
    v_tipo           tipo_espacio%ROWTYPE;
    v_motivo_bloqueo VARCHAR;
BEGIN
    -- Cancelar una reserva siempre se puede: solo se validan las confirmadas
    IF NEW.estado <> 'CONFIRMADA' THEN
        RETURN NEW;
    END IF;

    SELECT activo INTO v_activo FROM espacio WHERE id = NEW.espacio_id FOR UPDATE;
    IF NOT v_activo THEN
        RAISE EXCEPTION 'El espacio no está disponible';
    END IF;

    SELECT t.* INTO v_tipo
      FROM tipo_espacio t JOIN espacio e ON e.tipo_espacio_id = t.id
     WHERE e.id = NEW.espacio_id;

    IF NEW.fin - NEW.inicio <> make_interval(mins => v_tipo.duracion_min) THEN
        RAISE EXCEPTION 'Un turno de % dura % minutos', v_tipo.nombre, v_tipo.duracion_min;
    END IF;
    IF NEW.inicio::date <> NEW.fin::date
       OR NEW.inicio::time < v_tipo.hora_apertura
       OR NEW.fin::time > v_tipo.hora_cierre THEN
        RAISE EXCEPTION 'La reserva está fuera del horario de %', v_tipo.nombre;
    END IF;

    SELECT motivo INTO v_motivo_bloqueo
      FROM bloqueo
     WHERE espacio_id = NEW.espacio_id
       AND tsrange(inicio, fin) && tsrange(NEW.inicio, NEW.fin)
     LIMIT 1;
    IF FOUND THEN
        -- Mismo código de error que el EXCLUDE: para el backend es "turno no disponible"
        RAISE EXCEPTION 'El espacio está bloqueado: %', v_motivo_bloqueo
            USING ERRCODE = 'exclusion_violation';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_reserva_validar
    BEFORE INSERT OR UPDATE ON reserva
    FOR EACH ROW EXECUTE FUNCTION validar_reserva();


CREATE FUNCTION validar_bloqueo() RETURNS trigger AS $$
BEGIN
    PERFORM 1 FROM espacio WHERE id = NEW.espacio_id FOR UPDATE;

    IF NOT EXISTS (SELECT 1 FROM usuario WHERE id = NEW.creado_por AND rol = 'ADMIN') THEN
        RAISE EXCEPTION 'Solo un administrador puede bloquear espacios';
    END IF;

    -- Decisión del grupo: al bloquear, el backend cancela antes (en la misma
    -- transacción) las reservas futuras que caen en el bloqueo. Si quedara
    -- alguna confirmada, el bloqueo se rechaza. Las reservas que ya pasaron
    -- no se tocan: son historia.
    IF EXISTS (
        SELECT 1 FROM reserva
         WHERE espacio_id = NEW.espacio_id
           AND estado = 'CONFIRMADA'
           AND inicio > LOCALTIMESTAMP
           AND tsrange(inicio, fin) && tsrange(NEW.inicio, NEW.fin)
    ) THEN
        RAISE EXCEPTION 'Hay reservas confirmadas en ese horario: hay que cancelarlas antes de bloquear'
            USING ERRCODE = 'exclusion_violation';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_bloqueo_validar
    BEFORE INSERT OR UPDATE ON bloqueo
    FOR EACH ROW EXECUTE FUNCTION validar_bloqueo();


-- ---------------------------------------------------------------------------
-- Historial de eventos: RESPALDO de MongoDB
-- ---------------------------------------------------------------------------
-- El historial vive en MongoDB. Esta tabla es la segunda implementación del
-- mismo repositorio: si se configura PostgreSQL, o si MongoDB no responde, los
-- eventos se guardan acá. El detalle cambia según el tipo de evento, por eso es
-- JSONB (un documento JSON dentro de la columna) y no columnas fijas.
-- No tiene FK a usuario a propósito: es un registro de lo que pasó, igual que
-- el documento de Mongo, y no depende de las otras tablas.
CREATE TABLE historial_evento (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id  BIGINT      NOT NULL,
    tipo        VARCHAR(20) NOT NULL,
    fecha       TIMESTAMP   NOT NULL,
    detalle     JSONB       NOT NULL DEFAULT '{}',

    CONSTRAINT ck_historial_tipo
        CHECK (tipo IN ('RESERVA_CREADA', 'RESERVA_CANCELADA', 'RESERVA_RECHAZADA'))
);

-- "Mi actividad" trae los eventos de un usuario, del más nuevo al más viejo
CREATE INDEX ix_historial_usuario_fecha ON historial_evento (usuario_id, fecha DESC);


-- ---------------------------------------------------------------------------
-- Vista con la información relacionada de cada reserva (JOIN de 4 tablas)
-- ---------------------------------------------------------------------------
CREATE VIEW reserva_detalle AS
SELECT r.id,
       r.inicio,
       r.fin,
       r.estado,
       r.creada_en,
       r.cancelada_en,
       u.id                        AS usuario_id,
       u.mail                      AS usuario_mail,
       u.nombre || ' ' || u.apellido AS usuario_nombre,
       e.id                        AS espacio_id,
       e.nombre                    AS espacio_nombre,
       e.ubicacion                 AS espacio_ubicacion,
       t.nombre                    AS tipo_espacio
  FROM reserva r
  JOIN usuario u      ON u.id = r.usuario_id
  JOIN espacio e      ON e.id = r.espacio_id
  JOIN tipo_espacio t ON t.id = e.tipo_espacio_id;
