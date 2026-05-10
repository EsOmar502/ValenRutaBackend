===================== AUTENTICACIÓN (IMPORTANTE) =====================

La API utiliza JWT (Bearer Token).

============================ Paso 0: Registro ======================

POST
http://localhost:8080/api/auth/register

Body (JSON)
{
"nombre": "Carlos",
"email": "carlos@gmail.com",
"password": "123456"
}

Respuesta
{
"idUsuario": 1,
"nombre": "Carlos",
"email": "carlos@gmail.com"
}

-----------------------------------------------------------------------

============================ Paso 1: Login ======================

POST
http://localhost:8080/api/auth/login

Body (JSON)
{
"email": "carlos@email.com",
"password": "123456"
}

Respuesta
{
"token": "eyJhbGciOiJIUzI1NiJ9...",
"idUsuario": 1,
"nombre": "Carlos",
"email": "carlos@email.com"
}

-----------------------------------------------------------------------

=========================== Paso 2: Usar el token ======================

En Postman:

Ir a Authorization

Seleccionar Bearer Token

Pegar el token

Todas las rutas protegidas requieren esto.

=======================================================================
============================= 3. Usuarios ==============================

GET
GET /api/usuarios

Devuelve todos los usuarios.

=======================================================================
============================= 4. Vehículos ============================

Crear vehículo
POST
POST /api/vehiculos

Body
{
"marca": "Toyota",
"modelo": "Corolla",
"placa": "ABC123",
"color": "Negro",
"duenoId": 1
}

Validaciones:
- No permite duplicar vehículo con misma placa

=======================================================================
============================= 5. Viajes ===============================

Crear viaje
POST /api/viajes

Body
{
"origen": "Valencia",
"destino": "Barcelona",
"fecha": "2026-09-05",
"precio": 25,
"asientosDisponibles": 3,
"conductorId": 1
}

Validaciones:
- Precio > 0
- Asientos > 0
- No duplicar mismo viaje
- Se asigna estado automáticamente: PROGRAMADO

-----------------------------------------------------------------------

Obtener todos los viajes
GET /api/viajes

-----------------------------------------------------------------------

Obtener viaje por ID
GET /api/viajes/{id}

-----------------------------------------------------------------------

Buscar viajes cercanos
GET /api/viajes/cercanos?lat=39.47&lng=-0.38&radioKm=10

-----------------------------------------------------------------------

Finalizar viaje
PATCH /api/viajes/finalizar/{id}

Resultado:
- estado = FINALIZADO

-----------------------------------------------------------------------

Restricciones:
- Solo el conductor puede modificar/eliminar
- No se pueden reservar viajes FINALIZADOS

=======================================================================
============================= 6. Reservas ==============================

Crear reserva
POST /api/reservas

Body
{
"viajeId": 13,
"asientosReservados": 1
}

Validaciones:
- No reservar dos veces el mismo viaje
- No reservar más asientos de los disponibles
- No reservar tu propio viaje
- No reservar viajes FINALIZADOS

-----------------------------------------------------------------------

Estados de reserva:
- CONFIRMADA
- CANCELADA

-----------------------------------------------------------------------

Obtener mis reservas
GET /api/reservas/mis

-----------------------------------------------------------------------

Obtener reservas activas
GET /api/reservas/activas

-----------------------------------------------------------------------

Reservas de mis viajes (CONDUCTOR)
GET /api/reservas/conductor

Ejemplo respuesta:
{
"reservaId": 15,
"usuarioNombre": "Jesmy",
"usuarioEmail": "jesmy@email.com",
"asientos": 1,
"origen": "Valencia",
"destino": "Barcelona",
"fecha": "2026-09-05",
"estadoViaje": "PROGRAMADO"
}

-----------------------------------------------------------------------

Cancelar reserva
PATCH /api/reservas/cancelar/{id}

Resultado:
- estado = CANCELADA
- devuelve asientos automáticamente

-----------------------------------------------------------------------

Actualizar reserva
PUT /api/reservas/{id}

Body
{
"viajeId": 17
}

=======================================================================
============================= 7. Valoraciones ==========================

Crear valoración
POST /api/valoraciones

Body
{
"evaluadoId": 2,
"viajeId": 13,
"puntuacion": 5,
"comentario": "Excelente pasajero"
}

Reglas:
- No puedes valorarte a ti mismo
- Debes haber participado en el viaje
- Solo una valoración por viaje

-----------------------------------------------------------------------

Actualizar solo comentario
PUT /api/valoraciones/{id}/comentario

Body
{
"comentario": "Comentario actualizado"
}

-----------------------------------------------------------------------

Eliminar comentario
DELETE /api/valoraciones/{id}/comentario

Resultado:
- El comentario pasa a null

=======================================================================
============================= 8. Mensajes ==============================

Enviar mensaje
POST /api/mensajes

Body
{
"receptorId": 2,
"contenido": "Hola, ¿a qué hora salimos?"
}

Reglas:
- El emisor se obtiene del token
- La fecha se genera automáticamente
- No se puede enviar mensaje a uno mismo

-----------------------------------------------------------------------

Ver mis mensajes
GET /api/mensajes

Devuelve:
- Mensajes enviados
- Mensajes recibidos

=======================================================================
============================= 9. Manejo de errores =====================

Formato general:

{
"error": "Mensaje de error"
}

-----------------------------------------------------------------------

Errores de validación:

{
"error": "Error de validación",
"campos": {
"asientosReservados": "Debe ser mayor a 0"
}
}

=======================================================================
============================= RESUMEN ================================

Sistema:

- Usuarios autenticados con JWT
- Conductores crean viajes
- Usuarios reservan viajes
- Control de asientos
- Estados:
- Viaje → PROGRAMADO / FINALIZADO
- Reserva → CONFIRMADA / CANCELADA
- Cancelación con devolución de asientos
- Separación por roles:
- Usuario
- Conductor

