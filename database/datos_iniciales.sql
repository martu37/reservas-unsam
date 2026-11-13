-- Datos iniciales. Las personas, los tipos y los espacios son los de
-- database/dataset_inicial.csv, ya normalizados. Las reservas son otras: en la
-- planilla hay horarios que no son un turno del sistema nuevo (una parrilla de
-- 12 a 16), así que armamos unas parecidas con los turnos fijos.
-- Los id los genera SERIAL en el orden en que se insertan (1, 2, 3...).
-- Las fechas se calculan desde el día en que se carga la base, para que
-- siempre haya reservas próximas: CURRENT_DATE + 1 + TIME '18:00' es
-- "mañana a las 18".

-- 1 Juan, 2 Lucía, 3 Martín, 4 Sofía, 5 Daniela (admin), 6 Tomás, 7 Carla
INSERT INTO usuario (mail, apellido, nombre, claustro, rol) VALUES
    ('juan.perez@estudiantes.unsam.edu.ar',  'Pérez',     'Juan',    'ESTUDIANTE', 'USUARIO'),
    ('lucia.gomez@estudiantes.unsam.edu.ar', 'Gómez',     'Lucía',   'ESTUDIANTE', 'USUARIO'),
    ('mrodriguez@unsam.edu.ar',              'Rodríguez', 'Martín',  'DOCENTE',    'USUARIO'),
    ('sofia.lopez@estudiantes.unsam.edu.ar', 'López',     'Sofía',   'ESTUDIANTE', 'USUARIO'),
    ('dfernandez@unsam.edu.ar',              'Fernández', 'Daniela', 'NO_DOCENTE', 'ADMIN'),
    ('tomas.diaz@estudiantes.unsam.edu.ar',  'Díaz',      'Tomás',   'ESTUDIANTE', 'USUARIO'),
    ('cmartinez@unsam.edu.ar',               'Martínez',  'Carla',   'DOCENTE',    'USUARIO');

-- 1 fútbol 5, 2 fútbol 7, 3 parrilla
INSERT INTO tipo_espacio (nombre, duracion_min, hora_apertura, hora_cierre) VALUES
    ('Cancha fútbol 5', 60,  '09:00', '22:00'),
    ('Cancha fútbol 7', 90,  '09:00', '22:00'),
    ('Parrilla',        240, '10:00', '20:00');

-- 1 Cancha 1, 2 Cancha 2, 3 Cancha 3, 4 Parrilla 1, 5 Parrilla 2, 6 Parrilla 3
INSERT INTO espacio (nombre, tipo_espacio_id, ubicacion) VALUES
    ('Cancha 1',   1, 'Sector deportivo'),
    ('Cancha 2',   1, 'Sector deportivo'),
    ('Cancha 3',   2, 'Sector deportivo'),
    ('Parrilla 1', 3, 'Quincho norte'),
    ('Parrilla 2', 3, 'Quincho norte'),
    ('Parrilla 3', 3, 'Patio central');

INSERT INTO reserva (usuario_id, espacio_id, inicio, fin, estado, creada_en, cancelada_en) VALUES
    -- Hoy
    (5, 1, CURRENT_DATE + TIME '13:00', CURRENT_DATE + TIME '14:00', 'CONFIRMADA', CURRENT_DATE - 5 + TIME '10:15', NULL),
    (1, 1, CURRENT_DATE + TIME '21:00', CURRENT_DATE + TIME '22:00', 'CONFIRMADA', CURRENT_DATE - 5 + TIME '10:15', NULL),
    (2, 2, CURRENT_DATE + TIME '17:00', CURRENT_DATE + TIME '18:00', 'CONFIRMADA', CURRENT_DATE - 5 + TIME '10:15', NULL),
    -- Próximos días
    (1, 1, CURRENT_DATE + 1 + TIME '18:00', CURRENT_DATE + 1 + TIME '19:00', 'CONFIRMADA', CURRENT_DATE - 5 + TIME '10:15', NULL),
    (6, 2, CURRENT_DATE + 1 + TIME '18:00', CURRENT_DATE + 1 + TIME '19:00', 'CONFIRMADA', CURRENT_DATE - 5 + TIME '10:15', NULL),
    (3, 3, CURRENT_DATE + 1 + TIME '19:30', CURRENT_DATE + 1 + TIME '21:00', 'CONFIRMADA', CURRENT_DATE - 5 + TIME '10:15', NULL),
    (2, 4, CURRENT_DATE + 2 + TIME '10:00', CURRENT_DATE + 2 + TIME '14:00', 'CONFIRMADA', CURRENT_DATE - 5 + TIME '10:15', NULL),
    (4, 5, CURRENT_DATE + 2 + TIME '10:00', CURRENT_DATE + 2 + TIME '14:00', 'CANCELADA',  CURRENT_DATE - 5 + TIME '10:15', CURRENT_DATE + TIME '08:30'),
    (1, 2, CURRENT_DATE + 3 + TIME '18:00', CURRENT_DATE + 3 + TIME '19:00', 'CONFIRMADA', CURRENT_DATE - 5 + TIME '10:15', NULL),
    (7, 4, CURRENT_DATE + 4 + TIME '14:00', CURRENT_DATE + 4 + TIME '18:00', 'CONFIRMADA', CURRENT_DATE - 5 + TIME '10:15', NULL),
    -- Una que ya pasó
    (1, 1, CURRENT_DATE - 3 + TIME '18:00', CURRENT_DATE - 3 + TIME '19:00', 'CONFIRMADA', CURRENT_DATE - 5 + TIME '10:15', NULL);

-- Los dos los cargó Daniela (5), que es la admin
INSERT INTO bloqueo (espacio_id, inicio, fin, motivo, creado_por, creado_en) VALUES
    (6, CURRENT_DATE - 1 + TIME '00:00', CURRENT_DATE + 10 + TIME '00:00', 'Cambio de rejilla', 5, CURRENT_DATE - 2 + TIME '09:00'),
    (2, CURRENT_DATE + 2 + TIME '09:00', CURRENT_DATE + 2 + TIME '13:00', 'Corte de pasto',    5, CURRENT_DATE - 2 + TIME '09:00');
