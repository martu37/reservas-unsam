// Historial de eventos de cada usuario (MongoDB, colección "historial").
// Se carga solo la primera vez que se crea la base, igual que los .sql.
// Los eventos corresponden a las reservas de datos_iniciales.sql: cada reserva
// tiene su evento RESERVA_CREADA, la cancelada tiene además RESERVA_CANCELADA,
// y los pedidos rechazados solo existen acá (no son reservas).

db = db.getSiblingDB("reservas");

// Fecha relativa a hoy en hora argentina (UTC-3), como CURRENT_DATE + n en el SQL.
// dia(1, "18:00") es mañana a las 18.
function dia(n, hora) {
  const hoy = new Date(Date.now() - 3 * 60 * 60 * 1000);
  hoy.setUTCDate(hoy.getUTCDate() + n);
  const fecha = hoy.toISOString().slice(0, 10);
  return new Date(fecha + "T" + hora + ":00-03:00");
}

function creada(reservaId, usuarioId, espacio, n, desde, hasta) {
  return {
    usuarioId: usuarioId,
    tipo: "RESERVA_CREADA",
    fecha: dia(-5, "10:15"),
    detalle: { reservaId: reservaId, espacio: espacio, inicio: dia(n, desde), fin: dia(n, hasta) },
  };
}

function rechazada(usuarioId, fecha, espacio, n, desde, hasta, motivo) {
  return {
    usuarioId: usuarioId,
    tipo: "RESERVA_RECHAZADA",
    fecha: fecha,
    detalle: { espacio: espacio, inicio: dia(n, desde), fin: dia(n, hasta), motivo: motivo },
  };
}

db.historial.insertMany([
  // Una por cada reserva de datos_iniciales.sql (los ids van en el mismo orden)
  creada(1, 5, "Cancha 1", 0, "13:00", "14:00"),
  creada(2, 1, "Cancha 1", 0, "21:00", "22:00"),
  creada(3, 2, "Cancha 2", 0, "17:00", "18:00"),
  creada(4, 1, "Cancha 1", 1, "18:00", "19:00"),
  creada(5, 6, "Cancha 2", 1, "18:00", "19:00"),
  creada(6, 3, "Cancha 3", 1, "19:30", "21:00"),
  creada(7, 2, "Parrilla 1", 2, "10:00", "14:00"),
  creada(8, 4, "Parrilla 2", 2, "10:00", "14:00"),
  creada(9, 1, "Cancha 2", 3, "18:00", "19:00"),
  creada(10, 7, "Parrilla 1", 4, "14:00", "18:00"),
  creada(11, 1, "Cancha 1", -3, "18:00", "19:00"),

  // Sofía canceló la reserva 8 hoy a las 8:30, 49 horas antes del turno
  {
    usuarioId: 4,
    tipo: "RESERVA_CANCELADA",
    fecha: dia(0, "08:30"),
    detalle: {
      reservaId: 8,
      espacio: "Parrilla 2",
      inicio: dia(2, "10:00"),
      fin: dia(2, "14:00"),
      anticipacionHoras: 49,
    },
  },

  // Pedidos rechazados: turnos que ya tenía otro, y una parrilla en mantenimiento
  rechazada(3, dia(-4, "16:40"), "Cancha 1", 1, "18:00", "19:00", "El turno ya está reservado"),
  rechazada(7, dia(-2, "09:50"), "Cancha 2", 1, "18:00", "19:00", "El turno ya está reservado"),
  rechazada(2, dia(-3, "19:05"), "Cancha 1", 0, "21:00", "22:00", "El turno ya está reservado"),
  rechazada(6, dia(-1, "11:20"), "Parrilla 3", 3, "10:00", "14:00", "El espacio está bloqueado: Cambio de rejilla"),
]);

// La consulta de siempre es "el historial de un usuario, lo último primero".
// Es el mismo índice que ix_historial_usuario_fecha en PostgreSQL.
db.historial.createIndex({ usuarioId: 1, fecha: -1 });
