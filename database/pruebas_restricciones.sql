-- Pruebas de las restricciones del modelo.
-- Cada prueba intenta algo que la base tiene que rechazar (o aceptar) y dice
-- qué se espera. Lo que se acepta se borra al final de la prueba, así los
-- datos iniciales quedan como estaban.
-- Los id son los de datos_iniciales.sql (1 Juan, 2 Lucía... 1 Cancha 1...).
--
-- Cómo correrlo (con las bases levantadas):
--   docker exec -i reservas-postgres sh -c "psql -U reservas -d reservas 2>&1" < database/pruebas_restricciones.sql
-- (el 2>&1 junta los errores con el resto de la salida, para que salgan en orden)

\set VERBOSITY terse

\echo
\echo '== USUARIOS =='

\echo '1. Mail que no es de la universidad. Esperado: ERROR ck_usuario_mail'
INSERT INTO usuario (mail, apellido, nombre, claustro)
VALUES ('juan@gmail.com', 'Pérez', 'Juan', 'ESTUDIANTE');

\echo '2. Mail repetido. Esperado: ERROR uq_usuario_mail'
INSERT INTO usuario (mail, apellido, nombre, claustro)
VALUES ('juan.perez@estudiantes.unsam.edu.ar', 'Pérez', 'Juan', 'ESTUDIANTE');

\echo '3. Claustro que no existe. Esperado: ERROR ck_usuario_claustro'
INSERT INTO usuario (mail, apellido, nombre, claustro)
VALUES ('ana@unsam.edu.ar', 'Ruiz', 'Ana', 'GRADUADO');

\echo
\echo '== TIPOS Y ESPACIOS =='

\echo '4. Turno de 0 minutos. Esperado: ERROR ck_tipo_espacio_duracion'
INSERT INTO tipo_espacio (nombre, duracion_min, hora_apertura, hora_cierre)
VALUES ('Vóley', 0, '09:00', '20:00');

\echo '5. Cierra antes de abrir. Esperado: ERROR ck_tipo_espacio_horario'
INSERT INTO tipo_espacio (nombre, duracion_min, hora_apertura, hora_cierre)
VALUES ('Vóley', 60, '20:00', '09:00');

\echo '6. Espacio de un tipo que no existe. Esperado: ERROR fk_espacio_tipo'
INSERT INTO espacio (nombre, tipo_espacio_id, ubicacion)
VALUES ('Cancha 4', 99, 'Sector deportivo');

\echo '7. Borrar un tipo que tiene espacios. Esperado: ERROR fk_espacio_tipo'
DELETE FROM tipo_espacio WHERE id = 1;

\echo
\echo '== RESERVAS =='

\echo '8. Reservar un turno ya reservado (Cancha 1, mañana de 18 a 19). Esperado: ERROR uq_reserva_turno'
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES (2, 1, CURRENT_DATE + 1 + TIME '18:00', CURRENT_DATE + 1 + TIME '19:00');

\echo '9. El turno siguiente (Cancha 1, mañana de 19 a 20) está libre. Esperado: INSERT 0 1'
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES (2, 1, CURRENT_DATE + 1 + TIME '19:00', CURRENT_DATE + 1 + TIME '20:00');
DELETE FROM reserva WHERE espacio_id = 1 AND inicio = CURRENT_DATE + 1 + TIME '19:00';

\echo '10. La reserva cancelada de Sofía libera el turno (Parrilla 2, pasado mañana a las 10). Esperado: INSERT 0 1'
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES (6, 5, CURRENT_DATE + 2 + TIME '10:00', CURRENT_DATE + 2 + TIME '14:00');
DELETE FROM reserva WHERE espacio_id = 5 AND estado = 'CONFIRMADA';

\echo '11. Termina antes de empezar. Esperado: ERROR ck_reserva_fin'
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES (1, 1, CURRENT_DATE + 5 + TIME '19:00', CURRENT_DATE + 5 + TIME '18:00');

\echo '12. Estado que no existe. Esperado: ERROR ck_reserva_estado'
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin, estado)
VALUES (1, 1, CURRENT_DATE + 5 + TIME '18:00', CURRENT_DATE + 5 + TIME '19:00', 'PENDIENTE');

\echo '13. Cancelada sin fecha de cancelación. Esperado: ERROR ck_reserva_cancelada'
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin, estado)
VALUES (1, 1, CURRENT_DATE + 5 + TIME '18:00', CURRENT_DATE + 5 + TIME '19:00', 'CANCELADA');

\echo '14. Reserva de un usuario que no existe. Esperado: ERROR fk_reserva_usuario'
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES (99, 1, CURRENT_DATE + 5 + TIME '18:00', CURRENT_DATE + 5 + TIME '19:00');

\echo '15. Borrar a un usuario que tiene reservas. Esperado: ERROR fk_reserva_usuario'
DELETE FROM usuario WHERE id = 1;

\echo '16. Borrar un espacio que tiene reservas. Esperado: ERROR fk_reserva_espacio'
DELETE FROM espacio WHERE id = 1;

\echo
\echo '== BLOQUEOS =='

\echo '17. Bloqueo sin motivo. Esperado: ERROR ck_bloqueo_motivo'
INSERT INTO bloqueo (espacio_id, inicio, fin, motivo, creado_por)
VALUES (3, CURRENT_DATE + 5 + TIME '09:00', CURRENT_DATE + 5 + TIME '13:00', '', 5);

\echo '18. Bloqueo que termina antes de empezar. Esperado: ERROR ck_bloqueo_fin'
INSERT INTO bloqueo (espacio_id, inicio, fin, motivo, creado_por)
VALUES (3, CURRENT_DATE + 5 + TIME '13:00', CURRENT_DATE + 5 + TIME '09:00', 'Pintura', 5);

\echo '19. Borrar un espacio sin reservas borra sus bloqueos (CASCADE). Esperado: 0 bloqueos de Cancha 4'
INSERT INTO espacio (nombre, tipo_espacio_id, ubicacion) VALUES ('Cancha 4', 1, 'Sector deportivo');
INSERT INTO bloqueo (espacio_id, inicio, fin, motivo, creado_por)
VALUES ((SELECT id FROM espacio WHERE nombre = 'Cancha 4'),
        CURRENT_DATE + 5 + TIME '09:00', CURRENT_DATE + 5 + TIME '13:00', 'Pintura', 5);
DELETE FROM espacio WHERE nombre = 'Cancha 4';
SELECT count(*) AS bloqueos_de_cancha_4
  FROM bloqueo
 WHERE espacio_id NOT IN (SELECT id FROM espacio);

\echo
\echo '== Los datos quedaron como estaban =='
SELECT (SELECT count(*) FROM usuario) AS usuarios,
       (SELECT count(*) FROM espacio) AS espacios,
       (SELECT count(*) FROM reserva) AS reservas,
       (SELECT count(*) FROM bloqueo) AS bloqueos;
